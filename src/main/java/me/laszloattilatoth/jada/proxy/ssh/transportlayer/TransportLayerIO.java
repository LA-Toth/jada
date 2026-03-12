package me.laszloattilatoth.jada.proxy.ssh.transportlayer;

import me.laszloattilatoth.jada.proxy.ssh.core.SecureRandomWithByteArray;
import me.laszloattilatoth.jada.proxy.ssh.kex.NewKeys;
import me.laszloattilatoth.jada.proxy.ssh.kex.algorithm.CipherSpec;
import me.laszloattilatoth.jada.util.Logging;
import org.apache.sshd.common.util.buffer.Buffer;
import org.apache.sshd.common.util.buffer.ByteArrayBuffer;

import java.io.*;
import java.security.SecureRandom;
import java.util.logging.Logger;

public class TransportLayerIO implements TransportLayerInputOutput {
    private static final SecureRandom secureRandom = new SecureRandom();
    private boolean encryptedWriteMode = false;
    private boolean encryptedReadMode = false;
    protected Logger logger = null;
    protected NewKeys receiverNewKeys = new NewKeys();
    protected NewKeys senderNewKeys = new NewKeys();
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
    public void addReceiverNewKeys(NewKeys newKeys) {
    }

    @Override
    public void addSenderNewKeys(NewKeys newKeys) {
        encryptedReadMode = receiverNewKeys.cipherSpec == CipherSpec.CIPHER_NONE;
    }

    @Override
    public void sshMsgNewKeysSent() {
        encryptedWriteMode = senderNewKeys.cipherSpec == CipherSpec.CIPHER_NONE;
    }

    /**
     * Write packet as RFC 4253, 6.  Binary Packet Protocol
     */
    @Override
    public void writePacket(Packet packet) throws IOException {
        logger.info("Writing packet");
        packet.dump();
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
        System.out.printf("Padding length %d with hdrs %d payloadsize %d packet len %d%n", paddingLength, withHeaders, payloadSize, payloadSize + paddingLength + 1);

        int totalLength = withHeaders + paddingLength;

        ByteArrayBuffer buffer = new ByteArrayBuffer(totalLength);
        buffer.putInt(payloadSize + paddingLength + 1);
        buffer.putByte((byte) paddingLength);
        buffer.putRawBytes(bytes, 0, payloadSize);

        if (encryptedWriteMode) {
            SecureRandomWithByteArray secureRandomBA = new SecureRandomWithByteArray(payloadSize);
            buffer.putRawBytes(secureRandomBA.getSecureBytes());
        } else {
            for (int i = 0; i != paddingLength; ++i) {
                buffer.putByte((byte) 0);
            }
        }

        Logger logger = Logging.logger();
        logger.info(() -> String.format("Raw packet dump follows without MAC; length='%d'", totalLength));
        Logging.logBytes(logger, buffer.array(), totalLength);

        dataOutputStream.write(buffer.array(), 0, totalLength);

        // FIXME: mac
        dataOutputStream.flush();
    }

    protected int getPaddingLength(int payloadSize) {
        int withHeaders = payloadSize + 1 + 4;
        int blockSize = senderNewKeys.cipherBlockSize();
        int lessThenBlockSizeLen = withHeaders % blockSize;
        int paddingLength = 0;
        if (encryptedWriteMode) {
            // TODO
            paddingLength = Math.max(blockSize, Math.max(lessThenBlockSizeLen, secureRandom.nextInt(255))) / blockSize * blockSize;
        } else {
            // pointless to have any extra beyond to reach the block size
            // as this is unencrypted (except 4 bytes, as for some reason it's expected by OpenSSH, see below)
            if (lessThenBlockSizeLen > 0) {
                paddingLength = blockSize - lessThenBlockSizeLen;
            }
        }

        if (withHeaders + paddingLength < blockSize) {
            paddingLength += blockSize - withHeaders - paddingLength;
        }
        if (paddingLength < 4) {
            // fix for OpenSSH
            paddingLength += blockSize;
        }
        return paddingLength;
    }

    @Override
    public void sshMsgNewKeysReceived() {

    }

    @Override
    public Packet readPacket() throws TransportLayerException {
        try {
            if (encryptedReadMode) {
                return readEncryptedPacket();
            } else {
                return readClearTextPacket();
            }
        } catch (IOException e) {
            throw new TransportLayerException(e.getMessage());
        }
    }

    protected Packet readClearTextPacket() throws IOException {
        logger.info("Reading next packet");
        int packetLength = dataInputStream.readInt();
        byte paddingLength = dataInputStream.readByte();
        logger.info(() -> String.format("Read packet header; length='%d', hex_length='0x%x', padding_length='%d'",
                packetLength, packetLength, paddingLength));

        byte[] data = dataInputStream.readNBytes(packetLength - paddingLength - 1);
        logger.fine(() -> "Read packet data;");
        if (paddingLength > 0)
            dataInputStream.readNBytes(paddingLength);
        logger.fine(() -> "Read packet padding;");
        return new Packet(data);
    }

    protected Packet readEncryptedPacket() throws IOException {
        logger.info("Reading next encrypted packet");
        byte[] data = dataInputStream.readNBytes(receiverNewKeys.cipherBlockSize());

        return new Packet(data);
    }
}
