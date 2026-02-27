// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.core;

import me.laszloattilatoth.jada.config.ProxyConfig;
import me.laszloattilatoth.jada.util.Logging;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ProxyThread extends Thread {
    protected final int threadId;
    protected SocketChannel socketChannel;
    protected ProxyConfig config;
    protected Logger logger;

    public ProxyThread(SocketChannel socketChannel, ProxyConfig config, int threadId) {
        this.socketChannel = socketChannel;
        this.config = config;
        this.threadId = threadId;
        this.setName(String.format("%s:%d", config.name(), this.threadId));
        this.logger = createLogger();
    }

    private Logger createLogger() {
        return Logger.getLogger(getName());
    }

    public Logger logger() {
        return logger;
    }

    @Override
    public void run() {
        try {
            logger.info(String.format("Starting proxy instance; client_address='%s', client_local='%s'",
                    socketChannel.getRemoteAddress(), socketChannel.getLocalAddress()));
            this.runProxy();
            this.socketChannel.close();
        } catch (IOException e) {
            logger.severe(String.format("IOException occurred; message='%s'", e.getMessage()));
            Logging.logExceptionWithBacktrace(logger, e, Level.INFO);
        } finally {
            logger.info("Ending proxy instance;");
        }
    }

    protected abstract void runProxy() throws IOException;
}
