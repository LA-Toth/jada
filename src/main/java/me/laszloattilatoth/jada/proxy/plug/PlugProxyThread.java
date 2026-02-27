// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.plug;

import me.laszloattilatoth.jada.config.ProxyConfig;
import me.laszloattilatoth.jada.proxy.core.ProxyThread;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class PlugProxyThread extends ProxyThread {

    public PlugProxyThread(SocketChannel socketChannel, ProxyConfig config, int threadId) {
        super(socketChannel, config, threadId);
    }

    @Override
    protected void runProxy() throws IOException {
        try (SocketChannel serverSideSocket = SocketChannel.open(config.target())) {
            new PlugProxy(socketChannel, serverSideSocket, logger).run();
        }
    }
}
