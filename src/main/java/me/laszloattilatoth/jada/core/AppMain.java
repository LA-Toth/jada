/*
 * Copyright 2021-2026 Laszlo Attila Toth
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

package me.laszloattilatoth.jada.core;

import me.laszloattilatoth.jada.config.Config;
import me.laszloattilatoth.jada.config.ProxyConfig;
import me.laszloattilatoth.jada.proxy.core.ProxyMain;
import me.laszloattilatoth.jada.proxy.core.registration.Registrar;
import me.laszloattilatoth.jada.proxy.plug.PlugMain;
import me.laszloattilatoth.jada.proxy.socks.SocksMain;
import me.laszloattilatoth.jada.proxy.ssh.SshMain;
import me.laszloattilatoth.jada.util.Logging;
import me.laszloattilatoth.jada.util.Sec;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppMain {
    private final String configFileName;
    private final boolean verifyConfig;
    private final Logger logger;

    public AppMain(String configFileName, boolean verifyConfig, Logger logger) {
        this.configFileName = configFileName;
        this.verifyConfig = verifyConfig;
        this.logger = logger;
    }

    public void run() {
        PlugMain.setup();
        SocksMain.setup();
        SshMain.setup();

        if (!Sec.init()) {
            logger.severe("Unable to initialize security subsystem;");
            return;
        }
        Config config = null;
        try {
            config = Config.create(configFileName);
        } catch (FileNotFoundException | Config.InvalidConfig e) {
            Logging.logExceptionWithBacktrace(logger, e, Level.SEVERE);
            System.exit(1);
        }


        try {
            proxyLoop(config.proxyConfigs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void proxyLoop(List<ProxyConfig> configs) throws IOException {

        Selector selector = Selector.open();
        List<ProxyMain> proxyMains = new ArrayList<>();

        for (ProxyConfig cfg : configs) {
            ProxyMain m = null;
            try {
                m = Registrar.getRegistration(cfg.proxyType()).main().getDeclaredConstructor(cfg.getClass()).newInstance(cfg);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                e.printStackTrace();
                return;
            }
            proxyMains.add(m);
            m.registerToSelector(selector);
        }

        Logging.logger().info("Loaded proxies are ready for connections;");
        while (true) {
            if (selector.select() <= 0)
                continue;

            Set<SelectionKey> keys = selector.selectedKeys();
            for (SelectionKey key : keys) {
                if (key.isAcceptable()) {
                    ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                    SocketChannel socketChannel = serverSocketChannel.accept();

                    try {
                        for (ProxyMain m : proxyMains) {
                            if (m.hasServerSocketChannel(serverSocketChannel))
                                m.start(socketChannel);
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
            keys.clear();
        }
    }
}
