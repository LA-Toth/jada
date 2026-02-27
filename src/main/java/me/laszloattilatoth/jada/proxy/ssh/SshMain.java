// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh;

import me.laszloattilatoth.jada.config.ProxyConfig;
import me.laszloattilatoth.jada.proxy.core.ProxyMain;
import me.laszloattilatoth.jada.proxy.core.ProxyThread;
import me.laszloattilatoth.jada.proxy.core.registration.Registrar;

import java.nio.channels.SocketChannel;

public class SshMain extends ProxyMain {
    public SshMain(ProxyConfig config) {
        super(config);
    }

    public static void setup() {
        Registrar.registerProxy("ssh", SshMain.class, Options.class);
    }

    @Override
    public void start(SocketChannel channel) {
        ProxyThread t = new SshProxyThread(channel, config, nextThreadId());
        t.start();
    }
}
