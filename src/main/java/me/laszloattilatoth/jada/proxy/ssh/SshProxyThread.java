/*
 * Copyright 2020-2021 Laszlo Attila Toth
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

package me.laszloattilatoth.jada.proxy.ssh;

import me.laszloattilatoth.jada.config.ProxyConfig;
import me.laszloattilatoth.jada.proxy.core.ProxyThread;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.ClientSideTransportLayer;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.TransportLayer;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.TransportLayerException;
import me.laszloattilatoth.jada.util.Logging;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;

public class SshProxyThread extends ProxyThread {
    private final TransportLayer transportLayer;

    public SshProxyThread(SocketChannel socketChannel, ProxyConfig config, int threadId) {
        super(socketChannel, config, threadId);
        this.transportLayer = new ClientSideTransportLayer(this, socketChannel);
    }

    public final Options options() {
        return (Options) config.options();
    }

    @Override
    protected void runProxy() throws IOException {
        try {
            this.transportLayer.start();
        } catch (TransportLayerException e) {
            logger.severe(String.format("TransportLayerException occurred; message='%s'", e.getMessage()));
            Logging.logExceptionWithBacktrace(logger, e, Level.INFO);
        }
    }
}
