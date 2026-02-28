// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.transportlayer;

import me.laszloattilatoth.jada.proxy.ssh.SshProxyThread;
import me.laszloattilatoth.jada.proxy.ssh.core.Side;
import me.laszloattilatoth.jada.proxy.ssh.kex.KeyExchangeFactory;

import java.nio.channels.SocketChannel;

public class TransportLayerFactory {
    public TransportLayer create(SshProxyThread proxy, SocketChannel socketChannel, Side side) {
        if (side.isClient()) {
            return new TransportLayer(proxy, socketChannel, side, new KeyExchangeFactory());
            // This is hard-coded now, should be a configuration setting. Right now this class has to be recompiled.
            // return new StoringTransportLayer(proxy, socketChannel, side, new KeyExchangeFactory(true));
        }

        return null;
    }
}
