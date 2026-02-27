// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh;

import me.laszloattilatoth.jada.config.ProxyConfig;
import me.laszloattilatoth.jada.proxy.core.ProxyThread;
import me.laszloattilatoth.jada.proxy.ssh.core.Side;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.TransportLayer;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.TransportLayerException;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.TransportLayerFactory;
import me.laszloattilatoth.jada.util.Logging;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;

public class SshProxyThread extends ProxyThread {
    private final TransportLayer transportLayer;

    public SshProxyThread(SocketChannel socketChannel, ProxyConfig config, int threadId) {
        super(socketChannel, config, threadId);
        this.transportLayer = new TransportLayerFactory().create(this, socketChannel, Side.CLIENT);
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

    public boolean shouldQuit() {
        return false;
    }
}
