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

package me.laszloattilatoth.jada.proxy.socks;

import me.laszloattilatoth.jada.proxy.plug.PlugProxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

public abstract class SocksProxy {
    protected final SocketChannel socketChannel;
    protected final int threadId;
    protected final String name;
    private final ByteBuffer proxyBuffer = ByteBuffer.allocate(4096);
    protected Logger logger;

    SocksProxy(SocketChannel s, Logger logger, String name, int threadId) {
        this.socketChannel = s;
        this.logger = logger;
        this.threadId = threadId;
        this.name = name;
    }

    public static SocksProxy create(int version, SocketChannel s, Logger logger, String name, int threadId) {
        if (version == 4)
            return new SocksV4Proxy(s, logger, name, threadId);
        return new SocksV5Proxy(s, logger, name, threadId);
    }

    protected void connectAndTransfer(InetAddress address, short port) throws IOException {
        SocketChannel serverSideSocket = null;

        try {
            serverSideSocket = SocketChannel.open(new InetSocketAddress(address, port));
        } catch (IOException x) {
            sendConnectionFailureMsg();
            return;
        }
        try {
            sendConnectionSuccessMsg();
            new PlugProxy(socketChannel, serverSideSocket, logger).run();
        } finally {
            serverSideSocket.close();
        }
    }

    public abstract void run() throws IOException;

    protected abstract void sendConnectionSuccessMsg() throws IOException;

    protected abstract void sendConnectionFailureMsg() throws IOException;
}
