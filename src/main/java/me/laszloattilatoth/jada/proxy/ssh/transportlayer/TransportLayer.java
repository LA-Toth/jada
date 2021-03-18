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
import me.laszloattilatoth.jada.proxy.ssh.core.Constant;
import me.laszloattilatoth.jada.proxy.ssh.core.Side;
import me.laszloattilatoth.jada.util.Logging;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
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
    private final int macLength = 0;
    private final PacketHandler[] packetHandlers = new PacketHandler[256];
    private final String[] packetTypeNames = new String[256];
    private final List<Packet> replayPackets = new ArrayList<>();

    public TransportLayer(SshProxyThread proxy, SocketChannel socketChannel, Side side) {
        this.proxy = new WeakReference<>(proxy);
        this.logger = proxy.logger();
        this.socketChannel = socketChannel;
        this.side = side;
        this.setupHandlers();
    }

    private void setupHandlers() {
        for (int i = 0; i != packetHandlers.length; ++i)
            registerHandler(i, this::handleNotImplementedPacket, NOT_IMPLEMENTED_STR);

        registerHandler(Constant.SSH_MSG_DISCONNECT, this::processMsgIgnore, Constant.SSH_MSG_NAMES[Constant.SSH_MSG_DISCONNECT]);
        registerHandler(Constant.SSH_MSG_IGNORE, this::processMsgIgnore, Constant.SSH_MSG_NAMES[Constant.SSH_MSG_IGNORE]);
        registerHandler(Constant.SSH_MSG_UNIMPLEMENTED, this::processMsgUnimplemented, Constant.SSH_MSG_NAMES[Constant.SSH_MSG_UNIMPLEMENTED]);
        registerHandler(Constant.SSH_MSG_DEBUG, this::processMsgIgnore, Constant.SSH_MSG_NAMES[Constant.SSH_MSG_DEBUG]);
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

    /**
     * Starts the layer, aka. send / receive SSH-2.0... string
     * and do the first KEX
     */
    public void start() throws TransportLayerException {
        writeVersionString();
        readVersionString();
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
            ByteBuffer buffer = ByteBuffer.allocate(256);

            readVersionStringToBuffer(buffer);
            String versionString = new String(buffer.array());

            if (!versionString.startsWith("SSH-2.0")) {
                logger.severe(String.format("Unsupported SSH protocol; verson_string='%s'", versionString));
                throw new TransportLayerException("Unsupported SSH protocol");
            }

            logger.info("Remote ID String: " + versionString);
        } catch (
                IOException e) {
            Logging.logExceptionWithBacktrace(logger, e, Level.SEVERE);
        }
    }

    private void readVersionStringToBuffer(ByteBuffer byteBuffer) throws IOException {
        int readBytes;
        while (true) {
            readBytes = socketChannel.read(byteBuffer);

            if (readBytes < Constant.SSH_VERSION_STRING_PREFIX.length + 3) continue;

            boolean match = true;
            byte[] buffer = byteBuffer.array();
            for (int idx = 0; idx < Constant.SSH_VERSION_STRING_PREFIX.length; ++idx) {
                if (buffer[idx] != Constant.SSH_VERSION_STRING_PREFIX[idx]) {
                    match = false;
                    break;
                }
            }
            if (!match)
                continue;

            break;
        }
    }

    private void processMsgIgnore(Packet packet) {
    }

    private void processMsgUnimplemented(Packet packet) {
        byte packetType = packet.getType();
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
