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

import me.laszloattilatoth.jada.proxy.ssh.Options;
import me.laszloattilatoth.jada.proxy.ssh.core.Constant;
import me.laszloattilatoth.jada.proxy.ssh.core.Side;
import me.laszloattilatoth.jada.proxy.ssh.kex.KexException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.logging.Logger;

public abstract class WithTransportLayer {
    protected final WeakReference<TransportLayer> transportLayer;
    protected final Side side;
    protected final Logger logger;

    public WithTransportLayer(TransportLayer transportLayer) {
        this.transportLayer = new WeakReference<>(transportLayer);
        this.side = transportLayer.side;
        this.logger = transportLayer.getLogger();
    }

    protected Options options() {
        return Objects.requireNonNull(transportLayer).get().proxy().options();
    }

    protected void sendDisconnectMsg(long reasonCode, String reason) throws TransportLayerException {
        sendSingleDisconnectMsg(Objects.requireNonNull(transportLayer.get()), reasonCode, reason);
        // TODO: terminate other side, too
    }

    private void sendSingleDisconnectMsg(TransportLayer t, long reasonCode, String reason) throws TransportLayerException {
        Packet packet = new Packet();
        try {
            packet.putByte(Constant.SSH_MSG_DISCONNECT);
            packet.putUint32(reasonCode);
            packet.putString(reason);
            packet.putString("");
        } catch (Packet.BufferEndReachedException e) {
            logger.severe(() -> String.format("Unable to create SSH_MSG_DISCONNECT message; error='%s'", e.getMessage()));
            throw new TransportLayerException(e.getMessage());
        }

        try {
            t.writePacket(packet);
        } catch (IOException e) {
            logger.severe(() -> String.format("Unable to send (write) SSH_MSG_DISCONNECT message; error='%s'", e.getMessage()));
            throw new KexException(e.getMessage());
        }
    }
}
