// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.transportlayer;

import me.laszloattilatoth.jada.proxy.ssh.SshProxyThread;
import me.laszloattilatoth.jada.proxy.ssh.core.Side;
import me.laszloattilatoth.jada.proxy.ssh.kex.KeyExchangeFactory;

import java.nio.channels.SocketChannel;

public class ClientSideTransportLayer extends TransportLayer {
    public ClientSideTransportLayer(SshProxyThread proxy, SocketChannel socketChannel, KeyExchangeFactory keyExchangeFactory) {
        super(proxy, socketChannel, Side.CLIENT, keyExchangeFactory);
    }
}
