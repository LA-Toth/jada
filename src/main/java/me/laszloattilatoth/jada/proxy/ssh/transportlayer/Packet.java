// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.transportlayer;

import me.laszloattilatoth.jada.proxy.ssh.helpers.LoggerHelper;
import me.laszloattilatoth.jada.proxy.ssh.helpers.NameListHelper;
import me.laszloattilatoth.jada.util.Logging;
import org.apache.sshd.common.util.GenericUtils;
import org.apache.sshd.common.util.buffer.ByteArrayBuffer;

import java.util.logging.Logger;

/**
 * Representing an SSH packet with ByteBuffer.
 */
public class Packet extends ByteArrayBuffer {

    /** Creates an empty packet. */
    public Packet() {
        super();
    }

    /**
     * Creates a packet wrapping the given byte array.
     *
     * @param bytes the raw packet data
     */
    public Packet(byte[] bytes) {
        super(bytes);
    }

    /**
     * Returns {@code true} if there are no more bytes available to read.
     *
     * @return {@code true} when the read position has reached the end of the packet
     */
    public boolean endReached() {
        return available() == 0;
    }

    /**
     * Returns the SSH packet type as an unsigned integer read from the first byte of the packet.
     *
     * @return the packet type value (0–255)
     */
    public int packetType() {
         return array()[0] & 0xff;
    }

    /**
     * Returns a compacted copy of the written portion of this packet's internal buffer.
     * If no bytes have been written, returns an empty byte array.
     *
     * @return a byte array containing exactly the bytes that have been written
     */
    public byte[] getCompactArray() {
        int l = this.wpos();
        if (l > 0) {
            byte[] b = new byte[l];
            System.arraycopy(this.array(), 0, b, 0, l);
            return b;
        } else {
            return GenericUtils.EMPTY_BYTE_ARRAY;
        }
    }

    /**
     * Dumps the packet contents to the default application logger.
     */
    public void dump() {
        dump(Logging.logger());
    }

    /**
     * Dumps the packet type and raw bytes to the given logger at INFO level.
     *
     * @param logger the logger to write the dump to
     */
    public void dump(Logger logger) {
        logger.info(() -> String.format("Packet dump follows; packet_type='%d', packet_type_hex='%x', predefined_type_name='%s', length='%d'",
                packetType(), packetType(), LoggerHelper.packetTypeName(packetType()), wpos()));
        Logging.logBytes(logger, this.array(), this.wpos());
    }

    /**
     * Reads an SSH name-list from the current read position and returns the
     * corresponding algorithm/name identifier array.
     *
     * @return an array of name IDs parsed from the name-list string
     */
    public int[] getNameIdList() {
        return NameListHelper.getIdListFromNameArrayList(getNameList());
    }

    /**
     * Writes a single byte to the packet, accepting an {@code int} value for convenience.
     *
     * @param i the byte value to write (only the lowest 8 bits are used)
     */
    public void putByte(int i) {
        putByte((byte) i);
    }

    /**
     * Reads the next line from the packet, treating {@code \r\n} or {@code \n} as line
     * terminators. The read position is advanced past the terminator.
     * Returns {@code null} if no terminator is found or the packet is exhausted.
     *
     * @return the next line without its line terminator, or {@code null} if unavailable
     */
    public String getLine() {
        if (available() == 0)
            return null;
        final int rpos = rpos();
        final byte[] data = array();

        boolean found = false;
        int pos = rpos;
        for (; pos < wpos(); ++pos) {
            if (data[pos] == '\r' || data[pos] == '\n') {
                found = true;
                break;
            }
        }
        if (!found)
            return null;

        String result = new String(data, rpos(), pos - rpos());

        pos += (pos != rpos && data[pos] == '\r' && data[pos + 1] == '\n') ? 2 : 1;
        rpos(pos);
        return result;
    }
}
