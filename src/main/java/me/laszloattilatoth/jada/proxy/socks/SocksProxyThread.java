// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.socks;

import me.laszloattilatoth.jada.config.ProxyConfig;
import me.laszloattilatoth.jada.proxy.core.ProxyThread;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class SocksProxyThread extends ProxyThread {

    public SocksProxyThread(SocketChannel socketChannel, ProxyConfig config, int threadId) {
        super(socketChannel, config, threadId);
    }

    @Override
    protected void runProxy() throws IOException {
        socketChannel.configureBlocking(true);
        int version = socketChannel.socket().getInputStream().read();
        System.out.println(version);

        SocksProxy p = null;
        if (version == -1) {
            return;
        } else if (version == 4 || version == 5) {
            p = SocksProxy.create(version, socketChannel, logger, getName(), threadId);
        } else {
        }

        if (p != null) {
            p.run();
        }
    }
}
