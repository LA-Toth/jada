// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.kex;

import me.laszloattilatoth.jada.proxy.ssh.core.Side;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.TransportLayer;

public class KeyExchangeFactory {
    private boolean storePackets;

    public KeyExchangeFactory() {
        this(false);
    }

    public KeyExchangeFactory(boolean storePackets) {
        this.storePackets = storePackets;
    }

    public KeyExchange create(TransportLayer transportLayer, Side side) {
        if (side == Side.CLIENT) {
            if (storePackets) {
                return new StoringServerKeyExchange(transportLayer);
            } else {
                return new ServerKeyExchange(transportLayer);
            }
        }

        return null;
    }
}
