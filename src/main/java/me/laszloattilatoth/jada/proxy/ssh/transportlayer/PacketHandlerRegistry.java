// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.transportlayer;

import me.laszloattilatoth.jada.proxy.ssh.core.Constant;
import me.laszloattilatoth.jada.proxy.ssh.core.Side;
import me.laszloattilatoth.jada.proxy.ssh.helpers.LoggerHelper;

import java.util.logging.Logger;

/**
 * Maintains packet type to {@link PacketHandler} mappings used by the SSH transport layer.
 *
 * <p>The registry stores one handler and one display name per packet type value ({@code 0..255}).
 * Packet types that are not explicitly registered are initialized to a fallback
 * {@code notImplementedPacketHandler}.</p>
 */
public class PacketHandlerRegistry {
    /** Human-readable name used when no protocol name is known for a packet type. */
    private static final String UNKNOWN_STR = "(unknown)";
    /** Human-readable name used for packet types that currently have no implementation. */
    private static final String NOT_IMPLEMENTED_STR = "(not implemented)";

    /** Side (client/server) this registry belongs to. */
    public final Side side;
    /** Logger used for registration and debugging messages. */
    protected final Logger logger;
    /** Fallback handler used for all packet types without a dedicated handler. */
    private final PacketHandler notImplementedPacketHandler;
    /** Indexed by packet type; stores the effective handler for each packet value. */
    private final PacketHandler[] packetHandlers = new PacketHandler[256];
    /** Indexed by packet type; stores the readable packet type name for diagnostics. */
    private final String[] packetTypeNames = new String[256];

    /**
     * Creates a packet handler registry and initializes every packet type as not implemented.
     *
     * @param logger logger to use for registration messages
     * @param side transport side this registry belongs to
     * @param notImplementedPacketHandler fallback handler for unimplemented packet types
     */
    public PacketHandlerRegistry(Logger logger, Side side, PacketHandler notImplementedPacketHandler) {
        this.logger = logger;
        this.side = side;
        this.notImplementedPacketHandler = notImplementedPacketHandler;
        registerAllAsNotImplemented();
    }

    /**
     * Resets all packet types to the fallback "not implemented" handler.
     */
    public void registerAllAsNotImplemented() {
        for (int i = 0; i != packetHandlers.length; ++i)
            registerHandler(i, notImplementedPacketHandler, NOT_IMPLEMENTED_STR);
    }

    /**
     * Registers a handler and readable name for a specific packet type.
     *
     * @param packetType numeric packet type ({@code 0..255})
     * @param handler handler to invoke for the packet type
     * @param packetTypeName display name used in logs and diagnostics
     */
    public void registerHandler(int packetType, PacketHandler handler, String packetTypeName) {
        logger.info(() ->
                String.format("Registering packet handler for %s as %s (%d, 0x%x)",
                        LoggerHelper.packetTypeName(packetType),
                        packetTypeName, packetType, packetType));
        packetHandlers[packetType] = handler;
        packetTypeNames[packetType] = packetTypeName;
    }

    /**
     * Registers a handler using the protocol name from {@link Constant#SSH_MSG_NAMES}.
     *
     * @param packetType numeric packet type ({@code 0..255})
     * @param handler handler to invoke for the packet type
     */
    public void registerHandler(int packetType, PacketHandler handler) {
        registerHandler(packetType, handler, Constant.SSH_MSG_NAMES[packetType]);
    }

    /**
     * Unregisters a packet type by restoring the fallback "not implemented" handler.
     *
     * @param packetType numeric packet type ({@code 0..255})
     */
    public void unregisterHandler(int packetType) {
        registerHandler(packetType, notImplementedPacketHandler, NOT_IMPLEMENTED_STR);
    }

    /**
     * Returns the currently registered handler for a packet type.
     *
     * @param packetType numeric packet type ({@code 0..255})
     * @return handler currently mapped to the packet type
     */
    public PacketHandler getPacketHandler(int packetType) {
        return packetHandlers[packetType];
    }

    /**
     * Dispatches a packet using its own packet type value.
     *
     * @param packet packet to dispatch
     * @throws TransportLayerException when the selected handler fails
     */
    public void handlePacket(Packet packet) throws TransportLayerException {
        packetHandlers[packet.packetType()].handle(packet);
    }

    /**
     * Dispatches a packet using an explicit packet type index.
     *
     * @param packetType packet type used for handler lookup
     * @param packet packet to dispatch
     * @throws TransportLayerException when the selected handler fails
     */
    public void handlePacket(int packetType, Packet packet) throws TransportLayerException {
        packetHandlers[packetType].handle(packet);
    }

    /**
     * Returns the stored readable name for a packet type.
     *
     * @param packetType numeric packet type ({@code 0..255})
     * @return readable name for diagnostics
     */
    public String packetTypeName(int packetType) {
        return packetTypeNames[packetType];
    }
}
