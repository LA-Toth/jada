// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.transportlayer;

import me.laszloattilatoth.jada.proxy.core.LoggerHolder;
import me.laszloattilatoth.jada.proxy.ssh.core.Constant;
import me.laszloattilatoth.jada.proxy.ssh.core.Side;
import me.laszloattilatoth.jada.proxy.ssh.core.SshProxy;
import me.laszloattilatoth.jada.proxy.ssh.helpers.LoggerHelper;
import me.laszloattilatoth.jada.proxy.ssh.kex.KeyExchange;
import me.laszloattilatoth.jada.proxy.ssh.kex.KeyExchangeFactory;
import me.laszloattilatoth.jada.util.Logging;
import org.apache.sshd.common.util.buffer.Buffer;

import java.io.IOException;
import java.io.InputStream;
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
public class TransportLayer implements LoggerHolder {
    public final Side side;
    protected final Logger logger;
    protected final SocketChannel socketChannel;
    private final WeakReference<SshProxy> proxy;
    private final int macLength = 0;
    private final PacketHandlerRegistry packetHandlerRegistry;
    private final List<Packet> replayPackets = new ArrayList<>();
    protected KeyExchange kex;
    protected TransportLayerInputOutput io = null;
    protected int skipPackets = 0;
    private String peerIDString;

    public TransportLayer(SshProxy proxy, SocketChannel socketChannel, Side side, KeyExchangeFactory keyExchangeFactory) {
        this(proxy, socketChannel, side, new TransportLayerIO(proxy.logger()), keyExchangeFactory);
    }

    public TransportLayer(SshProxy proxy, SocketChannel socketChannel, Side side, TransportLayerInputOutput io, KeyExchangeFactory keyExchangeFactory) {
        this.proxy = new WeakReference<>(proxy);
        this.logger = proxy.logger();
        this.socketChannel = socketChannel;
        this.side = side;
        this.io = io;
        this.kex = keyExchangeFactory.create(this, side);
        this.packetHandlerRegistry = new PacketHandlerRegistry(logger, side, this::handleNotImplementedPacket);

        this.setupHandlers();
    }

    private void setupHandlers() {
        packetHandlerRegistry.registerHandler(Constant.SSH_MSG_DISCONNECT, this::processMsgIgnore);
        packetHandlerRegistry.registerHandler(Constant.SSH_MSG_IGNORE, this::processMsgIgnore);
        packetHandlerRegistry.registerHandler(Constant.SSH_MSG_UNIMPLEMENTED, this::processMsgUnimplemented);
        packetHandlerRegistry.registerHandler(Constant.SSH_MSG_DEBUG, this::processMsgIgnore);
        packetHandlerRegistry.registerHandler(Constant.SSH_MSG_KEXINIT, kex::processMsgKexInit);
    }

    public PacketHandlerRegistry getPacketHandlerRegistry() {
        return packetHandlerRegistry;
    }

    public void registerHandler(int packetType, PacketHandler handler, String packetTypeName) {
        packetHandlerRegistry.registerHandler(packetType, handler, packetTypeName);
    }

    public void unregisterHandler(int packetType) {
        packetHandlerRegistry.unregisterHandler(packetType);
    }

    @Override
    public Logger logger() {
        return logger;
    }

    public final SshProxy proxy() {
        return Objects.requireNonNull(proxy.get(), "Proxy cannot be nul in transport layer");
    }

    public void skipPackets(int count) {
        skipPackets = count;
    }

    public void skipPacket(byte packetType) {
        // TODO: implement
    }

    public String peerIDString() {
        return peerIDString;
    }

    public KeyExchange kex() {
        return kex;
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
        handlePacketsInLoop();
    }

    protected void writeVersionString() {
        logger.info(String.format("Sending version string; version='%s'", Constant.SSH_ID_STRING));
        try {
            socketChannel.write(ByteBuffer.wrap(String.format("%s\r\n", Constant.SSH_ID_STRING).getBytes()));
        } catch (IOException e) {
            logger.severe("Unable to send version string;");
            Logging.logException(logger, e, Level.INFO);
        }
    }

    protected void readVersionString() throws TransportLayerException {
        try {
            peerIDString = getVersionStringFromSocket();
            if (peerIDString == null)
                throw new TransportLayerException("No protocol version string is received");

            if (!peerIDString.startsWith("SSH-2.0")) {
                logger.severe(String.format("Unsupported SSH protocol; version_string='%s'", peerIDString));
                throw new TransportLayerException("Unsupported SSH protocol");
            }

            logger.info("Remote ID String: " + peerIDString);
        } catch (IOException e) {
            Logging.logExceptionWithBacktrace(logger, e, Level.SEVERE);
        }
    }

    private String getVersionStringFromSocket() throws IOException, TransportLayerException {
        InputStream is = socketChannel.socket().getInputStream();
        Packet pkt = new Packet();
        int data;
        boolean expectNL = false;
        int nonBannerLineCount = 0;
        String line;

        for (; nonBannerLineCount < Constant.MAX_PRE_BANNER_LINES; ++nonBannerLineCount) {
            pkt.clear(false);
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
                if (pkt.available() > Constant.MAX_BANNER_LENGTH) {
                    logger.severe(String.format(
                            "Too long Protocol Version String; truncated_length='%d', max='%d'",
                            pkt.available(), Constant.MAX_BANNER_LENGTH));
                    throw new TransportLayerException("Unable to read SSH protocol version string");
                }
            }
            if (pkt.available() <= 4)
                continue;
            pkt.putByte('\n');
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
            this.io.setInputStream(socketChannel.socket().getInputStream());
            this.io.setOutputStream(socketChannel.socket().getOutputStream());
        } catch (IOException e) {
            throw new TransportLayerException(e.getMessage());
        }
    }

    private void exchangeKeys() throws TransportLayerException {
        try {
            kex.sendInitialMsgKexInit();
            readAndHandlePacket();
        } catch (IOException e) {
            logger.severe("Unable to read packet;");
            Logging.logException(logger, e, Level.INFO);
            throw new TransportLayerException("Unable to read packet");
        }
    }

    public void readAndHandlePacket() throws IOException, TransportLayerException {
        Packet packet = this.io.readPacket();
        packet.dump();
        byte packetType = packet.packetType();
        boolean shouldSkip = skipPackets > 0;
        logger.info(() ->
                String.format("%s packet; type='%d', hex_type='%x', type_name='%s', predefined_type_name='%s', length='%d'",
                        (shouldSkip ? "Skipping" : "Processing"),
                        packetType, packetType,
                        LoggerHelper.packetTypeName(packetType),
                        packetHandlerRegistry.packetTypeName(packetType), packet.wpos()));

        if (shouldSkip) {
            --skipPackets;
            return;
        }

        if (false /* kex.getState() == KeyExchange.State.WAIT_FOR_OTHER_KEXINIT && packetType != Constant.SSH_MSG_KEXINIT */) {
            storePacket(packet);
        } else {
            packetHandlerRegistry.handlePacket(packetType, packet);
        }
    }

    /**
     * Write packet as RFC 4253, 6.  Binary Packet Protocol
     */
    public void writePacket(Packet packet) throws IOException {
        this.io.writePacket(packet);
    }

    /**
     * Write packet as RFC 4253, 6.  Binary Packet Protocol
     */
    public void writePacket(Buffer packet) throws IOException {
        this.io.writePacket(packet);
    }

    /**
     * Write packet as RFC 4253, 6.  Binary Packet Protocol
     */
    public void writePacket(byte[] bytes, int payloadSize) throws IOException {
        this.io.writePacket(bytes, payloadSize);
    }

    private void handlePacketsInLoop() throws TransportLayerException {
        try {
            while (!proxy().shouldQuit()) {
                readAndHandlePacket();
            }
        } catch (IOException e) {
            logger.severe("Unable to read packet;");
            Logging.logException(logger, e, Level.INFO);
            throw new TransportLayerException("Unable to read packet");
        }
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

    public void encryptionChange() {
        logger.info("Switching to encrypted mode");
    }
}
