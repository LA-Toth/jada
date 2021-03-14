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

import me.laszloattilatoth.jada.util.Logging;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;
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
            proxyMain(serverSideSocket);
        } finally {
            serverSideSocket.close();
        }
    }

    private void proxyMain(SocketChannel serverSideSocket) throws IOException {
        sendConnectionSuccessMsg();

        Selector selector = Selector.open();
        serverSideSocket.configureBlocking(false);
        socketChannel.configureBlocking(false);
        serverSideSocket.register(selector, SelectionKey.OP_READ);
        socketChannel.register(selector, SelectionKey.OP_READ);

        boolean shouldTerminate = false;
        while (!shouldTerminate) {
            int num = selector.select();
            if (num == 0) {
                continue;
            }

            Set<SelectionKey> keys = selector.selectedKeys();
            for (SelectionKey key : keys) {
                if (key.isReadable()) {
                    SocketChannel sc = (SocketChannel) key.channel();
                    shouldTerminate = !proxyInputToOtherSide(sc, serverSideSocket);
                }
            }
            keys.clear();
        }
    }

    private boolean proxyInputToOtherSide(SocketChannel channel, SocketChannel serverSideSocketChannel) throws IOException {
        boolean c2s = channel == socketChannel;
        SocketChannel other = channel == socketChannel ? serverSideSocketChannel : socketChannel;
        proxyBuffer.clear();
        int length = channel.read(proxyBuffer);
        if (length == 0) {
            return true;
        } else if (length == -1) {
            return false;
        }
        logger.info(String.format("Copying data to other side; side='%s', length='%d'", c2s ? "client" : "server", length));
        Logging.logBytes(logger, proxyBuffer.array(), proxyBuffer.position());
        proxyBuffer.flip();
        other.write(proxyBuffer);

        return true;
    }

    public abstract void run() throws IOException;

    protected abstract void sendConnectionSuccessMsg() throws IOException;

    protected abstract void sendConnectionFailureMsg() throws IOException;
}
