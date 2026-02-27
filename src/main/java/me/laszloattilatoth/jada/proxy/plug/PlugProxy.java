// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.plug;

import me.laszloattilatoth.jada.util.Logging;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.logging.Logger;

public class PlugProxy {
    private final ByteBuffer proxyBuffer = ByteBuffer.allocate(4096);
    private final SocketChannel socketChannel;
    private final SocketChannel serverSideSocketChannel;
    private final Logger logger;

    public PlugProxy(SocketChannel socketChannel, SocketChannel serverSideSocketChannel, Logger logger) {
        this.socketChannel = socketChannel;
        this.serverSideSocketChannel = serverSideSocketChannel;
        this.logger = logger;
    }

    public void run() throws IOException {
        Selector selector = Selector.open();
        serverSideSocketChannel.configureBlocking(false);
        socketChannel.configureBlocking(false);
        serverSideSocketChannel.register(selector, SelectionKey.OP_READ);
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
                    shouldTerminate = !proxyInputToOtherSide(sc);
                }
            }
            keys.clear();
        }
    }

    private boolean proxyInputToOtherSide(SocketChannel channel) throws IOException {
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
}
