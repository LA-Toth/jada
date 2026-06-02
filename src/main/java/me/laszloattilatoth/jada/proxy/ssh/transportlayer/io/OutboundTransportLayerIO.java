// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.transportlayer.io;

import me.laszloattilatoth.jada.proxy.ssh.core.SecureRandomWithByteArray;
import me.laszloattilatoth.jada.proxy.ssh.crypto.CryptoContext;
import me.laszloattilatoth.jada.proxy.ssh.crypto.CryptoContextPair;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.Packet;
import me.laszloattilatoth.jada.util.Logging;
import org.apache.sshd.common.util.buffer.Buffer;
import org.apache.sshd.common.util.buffer.ByteArrayBuffer;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.logging.Logger;

public class OutboundTransportLayerIO implements TransportLayerOutput {
    private static final SecureRandom secureRandom = new SecureRandom();
    protected Logger logger;
    protected CryptoContextPair outboundContextPair = new CryptoContextPair();
    private DataOutputStream dataOutputStream = null;

    public OutboundTransportLayerIO() {
        this(Logger.getGlobal());
    }

    public OutboundTransportLayerIO(Logger logger) {
        this.logger = logger;
    }


    @Override
    public void setOutputStream(OutputStream out) {
        this.dataOutputStream = new DataOutputStream(new BufferedOutputStream(out));
    }

    @Override
    public void addOutboundCryptoContext(CryptoContext context) {
        outboundContextPair.pending = context;
    }

    @Override
    public void sshMsgNewKeysSent() {
        outboundContextPair.current = outboundContextPair.pending;
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
}
