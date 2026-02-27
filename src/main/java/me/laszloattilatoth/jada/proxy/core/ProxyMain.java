// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.core;

import me.laszloattilatoth.jada.config.ProxyConfig;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ProxyMain {
    private final AtomicInteger lastThreadId = new AtomicInteger();
    protected ProxyConfig config;
    protected Map<ServerSocketChannel, Boolean> socketChannelMap = new HashMap<>();

    public ProxyMain(ProxyConfig config) {
        this.config = config;
    }

    protected int nextThreadId() {
        return lastThreadId.getAndIncrement();
    }

    public void registerToSelector(Selector selector) throws IOException {
        for (SocketAddress address : config.addresses()) {
            ServerSocketChannel ssc = ServerSocketChannel.open();
            ssc.configureBlocking(false);
            ssc.bind(address, 10);
            ssc.register(selector, SelectionKey.OP_ACCEPT);
            socketChannelMap.put(ssc, true);
        }
    }

    public boolean hasServerSocketChannel(ServerSocketChannel ssc) {
        return socketChannelMap.containsKey(ssc);
    }

    public void start(SocketChannel channel) {
    }
}
