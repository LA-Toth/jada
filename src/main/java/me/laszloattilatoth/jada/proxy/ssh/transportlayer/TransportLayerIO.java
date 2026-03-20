package me.laszloattilatoth.jada.proxy.ssh.transportlayer;

import me.laszloattilatoth.jada.proxy.ssh.core.Constant;
import me.laszloattilatoth.jada.proxy.ssh.core.SecureRandomWithByteArray;
import me.laszloattilatoth.jada.proxy.ssh.crypto.CryptoContext;
import me.laszloattilatoth.jada.proxy.ssh.crypto.CryptoContextPair;
import me.laszloattilatoth.jada.util.Logging;
import org.apache.sshd.common.util.buffer.Buffer;
import org.apache.sshd.common.util.buffer.ByteArrayBuffer;

import java.io.*;
import java.security.SecureRandom;
import java.util.logging.Logger;

public class TransportLayerIO implements TransportLayerInputOutput {
    private static final SecureRandom secureRandom = new SecureRandom();
    protected Logger logger = null;
    protected CryptoContextPair inboundContextPair = new CryptoContextPair();
    protected CryptoContextPair outboundContextPair = new CryptoContextPair();
    private DataInputStream dataInputStream = null;
    private DataOutputStream dataOutputStream = null;

    public TransportLayerIO() {
        this(Logger.getGlobal());
    }

    public TransportLayerIO(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void setInputStream(InputStream in) {
        this.dataInputStream = new DataInputStream(new BufferedInputStream(in));
    }

    @Override
    public void setOutputStream(OutputStream out) {
        this.dataOutputStream = new DataOutputStream(new BufferedOutputStream(out));
    }

    @Override
    public void addInboundCryptoContext(CryptoContext context) {
        inboundContextPair.pending = context;
    }

    @Override
    public void addOutboundCryptoContext(CryptoContext context) {
        outboundContextPair.pending = context;
    }

    @Override
    public void sshMsgNewKeysSent() {
        outboundContextPair.current = inboundContextPair.pending;
        outboundContextPair.pending = null;
    }

    /**
     * Write packet as RFC 4253, 6.  Binary Packet Protocol
     */
    @Override
    public void writePacket(Packet packet) throws IOException {
        logger.info("Writing packet");
        packet.dump(logger);
        writePacketBytes(packet.array(), packet.wpos());
    }

    /**
     * Write packet as RFC 4253, 6.  Binary Packet Protocol
     */
    @Override
    public void writePacket(Buffer packet) throws IOException {
        logger.info("Writing packet");
        Logger logger = Logging.logger();
        byte packetType = packet.array()[0];
        logger.info(() -> String.format("Packet dump follows; packet_type='%d', packet_type_hex='%x', length='%d'",
                packetType, packetType, packet.wpos()));
        Logging.logBytes(logger, packet.array(), packet.wpos());
        writePacketBytes(packet.array(), packet.wpos());
    }

    /**
     * Write packet as RFC 4253, 6.  Binary Packet Protocol
     */
    @Override
    public void writePacket(byte[] bytes, int payloadSize) throws IOException {
        logger.info("Writing packet");
        Logger logger = Logging.logger();
        logger.info(() -> String.format("Packet dump follows; packet_type='%d', packet_type_hex='%x', length='%d'",
                bytes[0], bytes[0], payloadSize));
        Logging.logBytes(logger, bytes, payloadSize);
        writePacketBytes(bytes, payloadSize);
    }

    protected void writePacketBytes(byte[] bytes, int payloadSize) throws IOException {
        int withHeaders = payloadSize + 1 + 4;
        int paddingLength = getPaddingLength(payloadSize);
        //System.out.printf("Padding length %d with hdrs %d payloadsize %d packet len %d%n", paddingLength, withHeaders, payloadSize, payloadSize + paddingLength + 1);

        int totalLength = withHeaders + paddingLength;

        ByteArrayBuffer buffer = new ByteArrayBuffer(totalLength);
        buffer.putInt(payloadSize + paddingLength + 1);
        buffer.putByte((byte) paddingLength);
        buffer.putRawBytes(bytes, 0, payloadSize);

        if (outboundContextPair.current != null) {
            SecureRandomWithByteArray secureRandomBA = new SecureRandomWithByteArray(payloadSize);
            buffer.putRawBytes(secureRandomBA.getSecureBytes());
        } else {
            for (int i = 0; i != paddingLength; ++i) {
                buffer.putByte((byte) 0);
            }
        }

        logger.info(() -> String.format("Raw packet dump follows without MAC; length='%d'", totalLength));
        Logging.logBytes(logger, buffer.array(), totalLength);

        dataOutputStream.write(buffer.array(), 0, totalLength);

        // FIXME: mac
        dataOutputStream.flush();
    }

    protected int getPaddingLength(int payloadSize) {
        final boolean encryptedWriteMode = outboundContextPair.current != null;

        int withHeaders = payloadSize + 1 + 4;
        int blockSize = encryptedWriteMode ? outboundContextPair.current.cipher().getBlockSize() : 8;
        int lessThenBlockSizeLen = withHeaders % blockSize;
        int paddingLength = 0;
        if (encryptedWriteMode) {
            // TODO
            paddingLength = Math.max(blockSize, Math.max(lessThenBlockSizeLen, secureRandom.nextInt(255))) / blockSize * blockSize;
        } else {
            // pointless to have any extra beyond to reach the block size
            if (lessThenBlockSizeLen > 0) {
                paddingLength = blockSize - lessThenBlockSizeLen;
            }
        }

        if (withHeaders + paddingLength < blockSize) {
            paddingLength += blockSize - withHeaders - paddingLength;
        }
        if (paddingLength < 4) {
            paddingLength += blockSize;
        }
        return paddingLength;
    }

    @Override
    public void sshMsgNewKeysReceived() {
        inboundContextPair.current = outboundContextPair.pending;
        inboundContextPair.pending = null;
    }

    @Override
    public Packet readPacket() throws TransportLayerException {
        try {
            if (inboundContextPair.current != null) {
                return readEncryptedPacket();
            } else {
                return readClearTextPacket();
            }
        } catch (IOException e) {
            throw new TransportLayerException(e.getMessage());
        }
    }

    protected Packet readClearTextPacket() throws IOException, TransportLayerException {
        logger.info("Reading next packet");
        int packetLength = dataInputStream.readInt();
        byte paddingLength = dataInputStream.readByte();
        logger.info(() -> String.format("Read packet header; length='%d', hex_length='0x%x', padding_length='%d'",
                packetLength, packetLength, paddingLength));

        if ((packetLength + 4) % Constant.CLEAR_TEXT_BLOCK_SIZE != 0) {
            throw new TransportLayerException("Read packet size is not multiple of block size");
        }

        if (paddingLength < 4) {
            throw new TransportLayerException("Padding length is smaller than the minimum value 4");
        }

        byte[] data = dataInputStream.readNBytes(packetLength - paddingLength - 1);
        logger.fine(() -> "Read packet data;");
        if (paddingLength > 0)
            dataInputStream.readNBytes(paddingLength);
        logger.fine(() -> "Read packet padding;");
        return new Packet(data);
    }

    protected Packet readEncryptedPacket() throws IOException {
        logger.info("Reading next encrypted packet");
        byte[] data = dataInputStream.readNBytes(inboundContextPair.current.cipher().getBlockSize());

        return new Packet(data);
    }
}
