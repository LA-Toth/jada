// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.core;

import me.laszloattilatoth.jada.config.Config;
import me.laszloattilatoth.jada.config.ProxyConfig;
import me.laszloattilatoth.jada.proxy.core.ProxyMain;
import me.laszloattilatoth.jada.util.Logging;
import me.laszloattilatoth.jada.util.Sec;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main runtime coordinator.
 *
 * <p>Initializes security, loads the YAML configuration, creates proxy runtimes and
 * dispatches accepted connections to the matching proxy implementation.
 */
public class AppMain {
    /**
     * Factory used to instantiate protocol-specific {@code ProxyMain} implementations.
     *
     * <p>This is initialized early so proxy registrations are available before config loading.
     */
    private static final ProxyFactory proxyFactory = new ProxyFactory();  // config loading requires registered proxies
    /** Path to the YAML configuration file. */
    private final String configFileName;
    /** Whether to only verify config (currently stored for future behavior). */
    private final boolean verifyConfig;
    /** Logger used for startup and runtime diagnostics. */
    private final Logger logger;

    /**
     * Create an application runtime entry point.
     *
     * @param configFileName path to the configuration file
     * @param verifyConfig whether to run in configuration-verification mode
     * @param logger logger used by this application instance
     */
    public AppMain(String configFileName, boolean verifyConfig, Logger logger) {
        this.configFileName = configFileName;
        this.verifyConfig = verifyConfig;
        this.logger = logger;
    }

    /**
     * Start the application.
     *
     * <p>Performs security initialization, loads configuration and enters the proxy accept loop.
     */
    public void run() {
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

    /**
     * Register all configured proxies on the selector and dispatch accepted sockets.
     *
     * @param configs validated proxy configurations loaded from YAML
     * @throws IOException if selector operations fail
     */
    private void proxyLoop(List<ProxyConfig> configs) throws IOException {

        Selector selector = Selector.open();
        List<ProxyMain> proxyMains = new ArrayList<>();

        for (ProxyConfig cfg : configs) {
            ProxyMain m = proxyFactory.createProxyMain(cfg, selector);
            if (m == null) return;

            proxyMains.add(m);
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
