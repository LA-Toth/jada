// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.transportlayer.io;

import me.laszloattilatoth.jada.proxy.ssh.core.Constant;
import me.laszloattilatoth.jada.proxy.ssh.crypto.CryptoContext;
import me.laszloattilatoth.jada.proxy.ssh.crypto.CryptoContextPair;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.Packet;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.TransportLayerException;

import javax.crypto.ShortBufferException;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.logging.Logger;

public class InboundTransportLayerIO implements TransportLayerInput {
    private static final SecureRandom secureRandom = new SecureRandom();
    protected Logger logger;
    protected CryptoContextPair inboundContextPair = new CryptoContextPair();
    private DataInputStream dataInputStream = null;

    public InboundTransportLayerIO() {
        this(Logger.getGlobal());
    }

    public InboundTransportLayerIO(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void setInputStream(InputStream in) {
        this.dataInputStream = new DataInputStream(new BufferedInputStream(in));
    }


    @Override
    public void addInboundCryptoContext(CryptoContext context) {
        inboundContextPair.pending = context;
    }

    @Override
    public void sshMsgNewKeysReceived() {
        inboundContextPair.current = inboundContextPair.pending;
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
        dataInputStream.readNBytes(paddingLength);
        logger.fine(() -> "Read packet padding;");
        return new Packet(data);
    }

    protected Packet readEncryptedPacket() throws IOException, TransportLayerException {
        logger.info("Reading next encrypted packet");
        byte[] data = dataInputStream.readNBytes(inboundContextPair.current.cipher().getBlockSize());
        byte[] output = new byte[inboundContextPair.current.cipher().getBlockSize()];
        try {
            this.inboundContextPair.current.cipher().update(data, 0, data.length, output);
        } catch (ShortBufferException e) {
            throw new TransportLayerException(e.getMessage());
        }


        return new Packet(output);
    }
}
