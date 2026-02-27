// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.core.registration;

import me.laszloattilatoth.jada.config.Config;
import me.laszloattilatoth.jada.config.ProxyOptions;
import me.laszloattilatoth.jada.proxy.core.ProxyMain;

public class Registrar {
    public static void registerProxy(String proxyType, Class<? extends ProxyMain> main, Class<? extends ProxyOptions> options) {
        Config.registerProxy(Registration.create(proxyType, main, options));
    }

    public static boolean supportedProxy(String proxyType) {
        return Config.supportedProxy(proxyType);
    }

    public static Registration getRegistration(String proxyType) {
        return Config.getProxyRegistration(proxyType);
    }
}
