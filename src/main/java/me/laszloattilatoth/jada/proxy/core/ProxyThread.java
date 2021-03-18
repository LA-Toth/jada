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
