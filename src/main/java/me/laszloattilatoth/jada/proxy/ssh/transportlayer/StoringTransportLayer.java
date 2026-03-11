// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.transportlayer;

import me.laszloattilatoth.jada.proxy.ssh.core.Constant;
import me.laszloattilatoth.jada.proxy.ssh.core.Side;
import me.laszloattilatoth.jada.proxy.ssh.core.SshProxy;
import me.laszloattilatoth.jada.proxy.ssh.kex.KeyExchangeFactory;
import me.laszloattilatoth.jada.util.Logging;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.logging.Level;

public class StoringTransportLayer extends TransportLayer {
    private static final String BASE_DIR = "/tmp/JADA/storing-transport-layer";
    private final int randomIdForStorage;
    private final String directory;
    private long outputFileCount = 0;

    public StoringTransportLayer(SshProxy proxy, SocketChannel socketChannel, Side side, KeyExchangeFactory keyExchangeFactory) {
        super(proxy, socketChannel, side, new StoringTransportLayerIO(), keyExchangeFactory);

        SecureRandom secureRandom = new SecureRandom();
        randomIdForStorage = secureRandom.nextInt();
        directory = BASE_DIR + "/" + randomIdForStorage;
        createDirectory();
    }

    private void createDirectory() {
        File directory = new File(this.directory);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    @Override
    protected void writeVersionString() {
        logger.info(String.format("Sending version string; version='%s'", Constant.SSH_ID_STRING));
        try {
            socketChannel.write(ByteBuffer.wrap(String.format("%s\r\n", Constant.SSH_ID_STRING).getBytes()));
            writeBytesToFile(String.format("%s\r\n", Constant.SSH_ID_STRING).getBytes(), true);
        } catch (IOException e) {
            logger.severe("Unable to send version string;");
            Logging.logException(logger, e, Level.INFO);
        }
    }

    @Override
    protected void readVersionString() throws TransportLayerException {
        super.readVersionString();
        byte[] bytes = peerIDString().getBytes();
        try {
            writeBytesToFile(bytes, bytes.length, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeBytesToFile(byte[] bytes, boolean out) throws IOException {
        writeBytesToFile(bytes, bytes.length, out);
    }

    public void writeBytesToFile(byte[] bytes, int count, boolean out) throws IOException {
        outputFileCount += 1;
        Path path = Paths.get(String.format(
                "%s/%03d.%s.bin",
                directory, outputFileCount, out ? "out" : "in"
        ));

        try (OutputStream os = Files.newOutputStream(path)) {
            os.write(bytes, 0, count);
        }
    }

    private static class StoringTransportLayerIO extends TransportLayerIO {

        private void writeBytesToFile(byte[] bytes, int count, boolean out) throws IOException {
            ((StoringTransportLayer) transportLayer()).writeBytesToFile(bytes, count, out);
        }

        @Override
        protected void writePacketBytes(byte[] bytes, int payloadSize) throws IOException {
            writeBytesToFile(bytes, payloadSize, true);
            super.writePacketBytes(bytes, payloadSize);
        }

        @Override
        protected Packet readClearTextPacket() throws IOException {
            Packet packet = super.readClearTextPacket();
            writeBytesToFile(packet.array(), packet.wpos(), false);
            return packet;
        }

        @Override
        protected Packet readEncryptedPacket() throws IOException {
            Packet packet = super.readEncryptedPacket();
            writeBytesToFile(packet.array(), packet.wpos(), false);
            return packet;
        }
    }
}
