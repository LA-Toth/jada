/*
 * Copyright 2021 Laszlo Attila Toth
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

package me.laszloattilatoth.jada.proxy;

import me.laszloattilatoth.jada.config.ProxyConfig;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

public class ProxyMain {
    protected ProxyConfig config;
    protected Map<ServerSocketChannel, Boolean> socketChannelMap = new HashMap<>();

    public ProxyMain(ProxyConfig config) {
        this.config = config;
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
