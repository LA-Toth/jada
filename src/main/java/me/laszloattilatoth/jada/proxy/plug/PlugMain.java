// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.plug;

import me.laszloattilatoth.jada.config.ProxyConfig;
import me.laszloattilatoth.jada.config.ProxyOptions;
import me.laszloattilatoth.jada.proxy.core.ProxyMain;
import me.laszloattilatoth.jada.proxy.core.ProxyThread;
import me.laszloattilatoth.jada.proxy.core.registration.Registrar;

import java.nio.channels.SocketChannel;

public class PlugMain extends ProxyMain {
    public PlugMain(ProxyConfig config) {
        super(config);
    }

    public static void setup() {
        Registrar.registerProxy("plug", PlugMain.class, ProxyOptions.class);
    }

    @Override
    public void start(SocketChannel channel) {
        ProxyThread t = new PlugProxyThread(channel, config, nextThreadId());
        t.start();
    }
}
