// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.transportlayer;

import me.laszloattilatoth.jada.proxy.ssh.SshProxyThread;
import me.laszloattilatoth.jada.proxy.ssh.core.Side;
import me.laszloattilatoth.jada.proxy.ssh.kex.ServerKeyExchange;

import java.nio.channels.SocketChannel;

public class ClientSideTransportLayer extends TransportLayer {
    public ClientSideTransportLayer(SshProxyThread proxy, SocketChannel socketChannel) {
        super(proxy, socketChannel, Side.CLIENT);
        this.kex = new ServerKeyExchange(this);
        this.setupHandlers();
    }
}
