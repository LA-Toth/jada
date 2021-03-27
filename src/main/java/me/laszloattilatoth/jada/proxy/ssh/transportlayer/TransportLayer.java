/*
 * Copyright 2020-2021 Laszlo Attila Toth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.laszloattilatoth.jada.proxy.ssh.transportlayer;

import me.laszloattilatoth.jada.proxy.ssh.SshProxyThread;
import me.laszloattilatoth.jada.proxy.ssh.core.Buffer;
import me.laszloattilatoth.jada.proxy.ssh.core.Constant;
import me.laszloattilatoth.jada.proxy.ssh.core.Side;
import me.laszloattilatoth.jada.proxy.ssh.kex.KeyExchange;
import me.laszloattilatoth.jada.util.Logging;

import java.io.*;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Based on RFC 4253 - The Secure Shell (SSH) Transport Layer Protocol
 */
public abstract class TransportLayer {
    private static final String UNKNOWN_STR = "(unknown)";
    private static final String NOT_IMPLEMENTED_STR = "(not implemented)";
    private static final String SSH_ID_STRING = "SSH-2.0-Jada";
    public final Side side;
    protected final Logger logger;
    protected final SocketChannel socketChannel;
    private final WeakReference<SshProxyThread> proxy;
    private final KeyExchange kex;
    private final int macLength = 0;
    private final PacketHandler[] packetHandlers = new PacketHandler[256];
    private final String[] packetTypeNames = new String[256];
    private final List<Packet> replayPackets = new ArrayList<>();
    protected DataInputStream dataInputStream = null;
    protected DataOutputStream dataOutputStream = null;

    public TransportLayer(SshProxyThread proxy, SocketChannel socketChannel, Side side) {
        this.proxy = new WeakReference<>(proxy);
        this.logger = proxy.logger();
        this.socketChannel = socketChannel;
        this.side = side;
        this.kex = new KeyExchange(this);
        this.setupHandlers();
    }

    private void setupHandlers() {
        for (int i = 0; i != packetHandlers.length; ++i)
            registerHandler(i, this::handleNotImplementedPacket, NOT_IMPLEMENTED_STR);

        registerHandler(Constant.SSH_MSG_DISCONNECT, this::processMsgIgnore, Constant.SSH_MSG_NAMES[Constant.SSH_MSG_DISCONNECT]);
        registerHandler(Constant.SSH_MSG_IGNORE, this::processMsgIgnore, Constant.SSH_MSG_NAMES[Constant.SSH_MSG_IGNORE]);
        registerHandler(Constant.SSH_MSG_UNIMPLEMENTED, this::processMsgUnimplemented, Constant.SSH_MSG_NAMES[Constant.SSH_MSG_UNIMPLEMENTED]);
        registerHandler(Constant.SSH_MSG_DEBUG, this::processMsgIgnore, Constant.SSH_MSG_NAMES[Constant.SSH_MSG_DEBUG]);
        registerHandler(Constant.SSH_MSG_KEXINIT, kex::processMsgKexInit, Constant.SSH_MSG_NAMES[Constant.SSH_MSG_KEXINIT]);
    }

    public void registerHandler(int packetType, PacketHandler handler, String packetTypeName) {
        packetHandlers[packetType] = handler;
        packetTypeNames[packetType] = packetTypeName;
    }

    public void unregisterHandler(int packetType) {
        registerHandler(packetType, this::handleNotImplementedPacket, NOT_IMPLEMENTED_STR);
    }

    public final Logger getLogger() {
        return logger;
    }

    public final SshProxyThread proxy() {
        return Objects.requireNonNull(proxy).get();
    }

    /**
     * Starts the layer, aka. send / receive SSH-2.0... string
     * and do the first KEX
     */
    public void start() throws TransportLayerException {
        writeVersionString();
        readVersionString();
        switchToDataStreams();
        exchangeKeys();
    }

    private void writeVersionString() {
        logger.info(String.format("Sending version string; version='%s'", SSH_ID_STRING));
        try {
            socketChannel.write(ByteBuffer.wrap(String.format("%s\r\n", SSH_ID_STRING).getBytes()));
        } catch (IOException e) {
            logger.severe("Unable to send version string;");
            Logging.logException(logger, e, Level.INFO);
        }
    }

    private void readVersionString() throws TransportLayerException {
        try {

            String versionString = getVersionStringFromSocket();
            if (versionString == null)
                throw new TransportLayerException("No protocol version string is received");

            if (!versionString.startsWith("SSH-2.0")) {
                logger.severe(String.format("Unsupported SSH protocol; version_string='%s'", versionString));
                throw new TransportLayerException("Unsupported SSH protocol");
            }

            logger.info("Remote ID String: " + versionString);
        } catch (
                IOException | Buffer.BufferEndReachedException e) {
            Logging.logExceptionWithBacktrace(logger, e, Level.SEVERE);
        }
    }

    private String getVersionStringFromSocket() throws IOException, Buffer.BufferEndReachedException, TransportLayerException {
        InputStream is = socketChannel.socket().getInputStream();
        Packet pkt = new Packet();
        int data;
        boolean expectNL = false;
        int nonBannerLineCount = 0;
        String line = null;

        for (; nonBannerLineCount < Constant.MAX_PRE_BANNER_LINES; ++nonBannerLineCount) {
            pkt.rewind();
            for (; ; ) {
                if ((data = is.read()) < 0)
                    return null;
                if (data == '\r') {
                    expectNL = true;
                    continue;
                } else if (data == '\n') {
                    break;
                } else if (data == '\0' || expectNL) {
                    logger.severe("Invalid Protocol Version String");
                    throw new TransportLayerException("Unable to read SSH protocol version string");
                }
                pkt.putByte(data);
                if (pkt.limit() > Constant.MAX_BANNER_LENGTH) {
                    logger.severe(String.format(
                            "Too long Protocol Version String; truncated_length='%d', max='%d'",
                            pkt.limit(), Constant.MAX_BANNER_LENGTH));
                    throw new TransportLayerException("Unable to read SSH protocol version string");
                }
            }
            if (pkt.limit() <= 4)
                continue;
            pkt.putByte('\n').flip();
            line = pkt.getLine();
            if (line.startsWith(Constant.SSH_VERSION_PREFIX))
                return line;
        }

        logger.severe(String.format(
                "Unable to read SSH protocol version string, too many previous lines; count='%s'", nonBannerLineCount));
        throw new TransportLayerException("Unable to read SSH protocol version string");
    }

    private void switchToDataStreams() throws TransportLayerException {
        try {
            dataInputStream = new DataInputStream(new BufferedInputStream(socketChannel.socket().getInputStream()));
            dataOutputStream = new DataOutputStream(new BufferedOutputStream(socketChannel.socket().getOutputStream()));
        } catch (IOException e) {
            throw new TransportLayerException(e.getMessage());
        }
    }

    private void exchangeKeys() throws TransportLayerException {
        try {
            kex.sendInitialMsgKexInit();
            readAndHandlePacket();
        } catch (IOException e) {
            logger.severe("Unable to read packet string;");
            Logging.logException(logger, e, Level.INFO);
            throw new TransportLayerException("Unable to read packet");
        }
    }

    public void readAndHandlePacket() throws IOException, TransportLayerException {
        Packet packet = readPacket();
        packet.dump();
        byte packetType = packet.getType();
        logger.info(() -> String.format("Processing packet; type='%d', hex_type='%x', type_name='%s', length='%d'",
                packetType, packetType, packetTypeNames[packetType], packet.limit()));

        if (kex.getState() == KeyExchange.State.WAIT_FOR_OTHER_KEXINIT && packetType != Constant.SSH_MSG_KEXINIT)
            storePacket(packet);
        else
            packetHandlers[packetType].handle(packet);
    }

    /**
     * Read packet as RFC 4253, 6.  Binary Packet Protocol
     */
    private Packet readPacket() throws IOException {
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

        if (macLength > 0)
            dataInputStream.readNBytes(macLength);

        return new Packet(data);
    }

    /**
     * Write packet as RFC 4253, 6.  Binary Packet Protocol
     */
    public void writePacket(Packet packet) throws IOException {
        logger.info("Writing packet");
        packet.dump();
        int payloadSize = packet.limit();
        int withHeaders = payloadSize + 1 + 4;
        int paddingLength = (withHeaders + 7) / 8 * 8 - withHeaders;

        dataOutputStream.writeInt(payloadSize + paddingLength + 1);
        dataOutputStream.writeByte(paddingLength);
        dataOutputStream.write(packet.array(), 0, payloadSize);
        // FIXME: secure padding
        for (int i = 0; i != paddingLength; ++i)
            dataOutputStream.writeByte(0);
        // FIXME: mac
        dataOutputStream.flush();
    }

    private void processMsgIgnore(Packet packet) {
    }

    private void processMsgUnimplemented(Packet packet) {
        byte packetType = packet.packetType();
        logger.info(() -> String.format("Processing unimplemented packet; type='%d', hex_type='%x'",
                packetType, packetType));
    }

    private void handleNotImplementedPacket(Packet packet) {
        // TODO: what to do if a packet is not handled
    }

    private void storePacket(Packet packet) {
        logger.info(() -> "Waiting for SSH_MSG_KEXINIT from other side, storing packet;");
        replayPackets.add(packet);
    }
}
