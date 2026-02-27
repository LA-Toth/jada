// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.plug;

import me.laszloattilatoth.jada.config.ProxyConfig;
import me.laszloattilatoth.jada.proxy.core.ProxyMain;

public class PlugMain extends ProxyMain {
    public PlugMain(ProxyConfig config) {
        super(config, PlugProxyThread.class);
    }
}
