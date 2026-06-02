// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.core;

import me.laszloattilatoth.jada.config.ProxyConfig;
import me.laszloattilatoth.jada.config.ProxyOptions;
import me.laszloattilatoth.jada.proxy.core.ProxyMain;
import me.laszloattilatoth.jada.proxy.core.registration.Registrar;
import me.laszloattilatoth.jada.proxy.plug.PlugMain;
import me.laszloattilatoth.jada.proxy.socks.Options;
import me.laszloattilatoth.jada.proxy.socks.SocksMain;
import me.laszloattilatoth.jada.proxy.ssh.SshMain;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.Selector;

/**
 * Creates protocol-specific {@link ProxyMain} instances from validated {@link ProxyConfig} data.
 *
 * <p>The static initializer registers built-in proxy types in the central registration registry.
 */
public class ProxyFactory {
    static {
        Registrar.registerProxy("plug", PlugMain.class, ProxyOptions.class);
        Registrar.registerProxy("socks", SocksMain.class, Options.class);
        Registrar.registerProxy("ssh", SshMain.class, me.laszloattilatoth.jada.proxy.ssh.Options.class);
    }

    /**
     * Build and register a proxy runtime for the given configuration.
     *
     * @param cfg proxy configuration describing type, listeners, target and options
     * @param selector shared selector used by all proxy runtimes
     * @return initialized proxy runtime, or {@code null} when reflective construction fails
     * @throws IOException if selector registration fails
     */
    public ProxyMain createProxyMain(ProxyConfig cfg, Selector selector) throws IOException {
        ProxyMain m = null;
        try {
            m = Registrar.getRegistration(cfg.proxyType()).main().getDeclaredConstructor(cfg.getClass()).newInstance(cfg);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
        m.registerToSelector(selector);
        return m;
    }
}
