package me.laszloattilatoth.jada.proxy.ssh.core;

import me.laszloattilatoth.jada.util.Logging;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a byte buffer with put/get methods of basic and SSH types.
 * <p>
 * SSH has its own types like string.
 */
public class Buffer<T extends Buffer<T>> {
    public static final int MAX_SIZE = 0x8000000; // as in OpenSSH buffer
    public static final int DEFAULT_INIT_SIZE = 256;
    public static final int DEFAULT_INC_SIZE = 256;
    protected final int incSize;
    protected final int maxSize;
    protected byte[] buffer;
    protected int position = 0;
    protected int limit;   // size of valid data (buffer may be larger)

    public Buffer(byte[] bytes) {
        this.buffer = bytes;
        this.maxSize = MAX_SIZE;
        this.limit = this.buffer.length;
        this.incSize = DEFAULT_INC_SIZE;
    }

    public Buffer() {
        this(DEFAULT_INIT_SIZE, DEFAULT_INC_SIZE, MAX_SIZE);
    }

    protected Buffer(int allocationSize, int incSize, int maxSize) {
        this.buffer = new byte[allocationSize];
        this.incSize = incSize;
        this.maxSize = maxSize;
        this.limit = 0;
    }

    public final int mark() {
        return 0;
    }

    public final int limit() {
        return limit;
    }

    public final int capacity() {
        return buffer.length;
    }

    public int position() {
        return position;
    }

    public boolean limitReached() {
        return position == limit();
    }

    public final int incSize() {
        return incSize;
    }

    public final int maxSize() {
        return maxSize;
    }

    @SuppressWarnings("unchecked")
    public T resetPosition() {
        position = 0;
        return (T) this;
    }

    public T rewind() {
        return resetPosition();
    }

    @SuppressWarnings("unchecked")
    public T flip() {
        limit = position;
        position = 0;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T clear() {
        limit = 0;
        position = 0;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T appendByteBuffer(ByteBuffer byteBuffer) throws BufferEndReachedException {
        int prevPos = position;
        try {
            putBytes(byteBuffer.array(), 0, byteBuffer.position());
        } finally {
            position = prevPos;
        }
        return (T) this;
    }

    protected void checkPosition(int requiredLength) throws BufferEndReachedException {
        if (position + requiredLength > limit) {
            Logging.logger().severe(String.format("Unable to read required bytes from packet; required='%d'", requiredLength));
            throw new BufferEndReachedException("Unable to read required bytes from packet");
        }
    }

    protected void preserve(int requiredLength) throws BufferEndReachedException {
        if (requiredLength > maxSize || maxSize - requiredLength < position) {
            Logging.logger().severe(String.format("Unable to allocate required bytes into packet; required='%d'", requiredLength));
            throw new BufferEndReachedException("Unable to allocate bytes in packet");
        }

        if (limit + requiredLength < buffer.length)
            return;

        int newSize = Math.min(maxSize, ((limit + requiredLength + incSize - 1) / incSize) * incSize);
        byte[] newBuffer = new byte[newSize];

        System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
        buffer = newBuffer;
    }

    public byte[] array() {
        return buffer;
    }

    public byte[] arrayCopy() {
        byte[] copy = new byte[limit];
        System.arraycopy(buffer, 0, copy, 0, limit);
        return copy;
    }

    public void dump() {
        Logging.logBytes(Logging.logger(), buffer, limit);
    }

    public byte getType() {
        return buffer[0];
    }

    public int getByte() throws BufferEndReachedException {
        checkPosition(1);
        return buffer[position++] & 0xff;
    }

    private int getByteUnchecked() {
        return buffer[position++] & 0xff;
    }

    public byte[] getBytes(int length) throws BufferEndReachedException {
        checkPosition(length);
        byte[] result = new byte[length];
        System.arraycopy(buffer, position, result, 0, length);
        position += length;
        return result;
    }

    /* getX: based on RFC 2451 5.  Data Type Representations Used in the SSH Protocols */

    public boolean getBoolean() throws BufferEndReachedException {
        return getByte() != 0;
    }

    public int getUint32() throws BufferEndReachedException {
        checkPosition(4);
        logBytes(4);
        return ((getByteUnchecked() << 24) + (getByteUnchecked() << 16) + (getByteUnchecked() << 8) + getByteUnchecked());
    }

    protected void logBytes(int length) {
        Logger logger = Logging.logger();
        if (!logger.isLoggable(Level.FINEST))
            return;

        logger.finest(String.format("Log packet bytes; position='%d', hex_pos='%04x', count='%d'", position, position, length));
        for (int i = 0; i != length; ++i) {
            int val = buffer[position + i] & 0xff;
            logger.finest(String.format("Packet byte; hex='%02x', dec='%d', val='%c', offset='%d'",
                    val, val,
                    (val < 32 || val > 126) ? '.' : val,
                    i));
        }
    }

    public long getUint32AsLong() throws BufferEndReachedException {
        checkPosition(4);
        logBytes(4);
        return (((long) getByteUnchecked() << 24) + ((long) getByteUnchecked() << 16) + ((long) getByteUnchecked() << 8) + ((long) getByteUnchecked()));
    }

    public long getUint64() throws BufferEndReachedException {
        checkPosition(8);
        logBytes(8);
        return (((long) getByteUnchecked() << 56) + ((long) getByteUnchecked() << 48) +
                ((long) getByteUnchecked() << 40) + ((long) getByteUnchecked() << 32) +
                ((long) getByteUnchecked() << 24) + ((long) getByteUnchecked() << 16) +
                ((long) getByteUnchecked() << 8) + ((long) getByteUnchecked()));
    }

    public String getSshString() throws BufferEndReachedException {
        int length = getUint32();
        checkPosition(length);

        String s = new String(buffer, position, length);
        position += length;
        return s;
    }

    public byte[] getMpInt() throws BufferEndReachedException {
        int length = getUint32();
        checkPosition(length);

        byte[] b = Arrays.copyOfRange(buffer, position, length);
        position += length;
        return b;
    }

    @SuppressWarnings("unchecked")
    public T resetType(byte packetType) {
        resetPosition();
        buffer[0] = packetType;
        return (T) this;
    }

    private void putByteUnchecked(byte b) {
        buffer[position++] = b;
        if (limit < position)
            limit++;
    }

    @SuppressWarnings("unchecked")
    public T putByte(int b) throws BufferEndReachedException {
        preserve(1);
        putByteUnchecked((byte) b);
        return (T) this;
    }

    public T putBoolean(boolean b) throws BufferEndReachedException {
        return putByte(b ? 1 : 0);
    }

    @SuppressWarnings("unchecked")
    public T putBytes(byte[] b) throws BufferEndReachedException {
        preserve(b.length);
        System.arraycopy(b, 0, buffer, position, b.length);
        position += b.length;
        if (limit < position)
            limit = position;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T putBytes(byte[] b, int offset, int count) throws BufferEndReachedException {
        preserve(count);
        System.arraycopy(b, offset, buffer, position, count);
        position += count;
        limit += count;
        return (T) this;
    }

    public T putByteBuffer(ByteBuffer byteBuffer) throws BufferEndReachedException {
        return putBytes(byteBuffer.array(), 0, byteBuffer.position());
    }

    @SuppressWarnings("unchecked")
    public T putUint32(long l) throws BufferEndReachedException {
        preserve(4);
        putByteUnchecked((byte) (l >> 24));
        putByteUnchecked((byte) (l >> 16));
        putByteUnchecked((byte) (l >> 8));
        putByteUnchecked((byte) l);
        return (T) this;
    }

    @SuppressWarnings("unchecked")

    public T putUint64(long l) throws BufferEndReachedException {
        preserve(8);
        putByteUnchecked((byte) (l >> 56));
        putByteUnchecked((byte) (l >> 48));
        putByteUnchecked((byte) (l >> 40));
        putByteUnchecked((byte) (l >> 32));
        putByteUnchecked((byte) (l >> 24));
        putByteUnchecked((byte) (l >> 16));
        putByteUnchecked((byte) (l >> 8));
        putByteUnchecked((byte) l);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T putString(String s) throws BufferEndReachedException {
        byte[] bytes = s.getBytes();
        preserve(4 + bytes.length);
        putUint32(bytes.length);
        putBytes(bytes);
        return (T) this;
    }

    public static final class BufferEndReachedException extends Exception {
        BufferEndReachedException(String s) {
            super(s);
        }
    }
}
