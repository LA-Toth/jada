// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.transportlayer;

import me.laszloattilatoth.jada.proxy.ssh.Options;
import me.laszloattilatoth.jada.proxy.ssh.core.Constant;
import me.laszloattilatoth.jada.proxy.ssh.core.Side;
import me.laszloattilatoth.jada.proxy.ssh.kex.KexException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.logging.Logger;

public abstract class WithTransportLayer {
    protected final Side side;
    protected final Logger logger;
    private final WeakReference<TransportLayer> transportLayer;

    public WithTransportLayer(TransportLayer transportLayer) {
        this.transportLayer = new WeakReference<>(transportLayer);
        this.side = transportLayer.side;
        this.logger = transportLayer.getLogger();
    }

    public TransportLayer transportLayer() {
        return Objects.requireNonNull(transportLayer.get(), "transport layer cannot be null");
    }

    protected Options options() {
        return transportLayer().proxy().options();
    }

    protected void sendDisconnectMsg(long reasonCode, String reason) throws TransportLayerException {
        sendSingleDisconnectMsg(Objects.requireNonNull(transportLayer.get()), reasonCode, reason);
        // TODO: terminate other side, too
    }

    private void sendSingleDisconnectMsg(TransportLayer t, long reasonCode, String reason) throws TransportLayerException {
        Packet packet = new Packet();
        packet.putByte(Constant.SSH_MSG_DISCONNECT);
        packet.putInt(reasonCode);
        packet.putString(reason);
        packet.putString("");

        try {
            t.writePacket(packet);
        } catch (IOException e) {
            logger.severe(() -> String.format("Unable to send (write) SSH_MSG_DISCONNECT message; error='%s'", e.getMessage()));
            throw new KexException(e.getMessage());
        }
    }
}
