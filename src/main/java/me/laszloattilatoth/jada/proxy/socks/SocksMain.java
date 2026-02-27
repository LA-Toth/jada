// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.socks;

import me.laszloattilatoth.jada.config.ProxyConfig;
import me.laszloattilatoth.jada.proxy.core.ProxyMain;

public class SocksMain extends ProxyMain {
    public SocksMain(ProxyConfig config) {
        super(config, SocksProxyThread.class);
    }
}
