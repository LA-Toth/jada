/*
 * Copyright 2020-2026 Laszlo Attila Toth
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

import me.laszloattilatoth.jada.proxy.ssh.core.Constant;
import me.laszloattilatoth.jada.proxy.ssh.core.Side;
import me.laszloattilatoth.jada.proxy.ssh.helpers.LoggerHelper;

import java.util.logging.Logger;

/**
 * Tracks the registered PacketHandler methods as a part of the TransportLayer
 */
public class PacketHandlerRegistry {
    private static final String UNKNOWN_STR = "(unknown)";
    private static final String NOT_IMPLEMENTED_STR = "(not implemented)";
    public final Side side;
    private final PacketHandler notImplementedPacketHandler;
    protected final Logger logger;
    private final PacketHandler[] packetHandlers = new PacketHandler[256];
    private final String[] packetTypeNames = new String[256];

    public PacketHandlerRegistry(Logger logger, Side side, PacketHandler notImplementedPacketHandler) {
        this.logger = logger;
        this.side = side;
        this.notImplementedPacketHandler = notImplementedPacketHandler;
        registerAllAsNotImplemented();
    }

    public void registerAllAsNotImplemented() {
        for (int i = 0; i != packetHandlers.length; ++i)
            registerHandler(i, notImplementedPacketHandler, NOT_IMPLEMENTED_STR);
    }

    public void registerHandler(int packetType, PacketHandler handler, String packetTypeName) {
        logger.info(() ->
                String.format("Registering packet handler for %s as %s (%d, 0x%x)",
                        LoggerHelper.packetTypeName(packetType),
                        packetTypeName, packetType, packetType));
        packetHandlers[packetType] = handler;
        packetTypeNames[packetType] = packetTypeName;
    }

    public void registerHandler(int packetType, PacketHandler handler) {
        registerHandler(packetType, handler, Constant.SSH_MSG_NAMES[packetType]);
    }

    public void unregisterHandler(int packetType) {
        registerHandler(packetType, notImplementedPacketHandler, NOT_IMPLEMENTED_STR);
    }

    public PacketHandler getPacketHandler(int packetType) {
        return packetHandlers[packetType];
    }

    public void handlePacket(Packet packet) throws TransportLayerException {
        packetHandlers[packet.packetType()].handle(packet);
    }

    public void handlePacket(byte packetType, Packet packet) throws TransportLayerException {
        packetHandlers[packetType].handle(packet);
    }

    public String packetTypeName(int packetType) {
        return packetTypeNames[packetType];
    }
}
