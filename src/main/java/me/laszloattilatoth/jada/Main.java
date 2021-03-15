/*
 * Copyright 2021 Laszlo Attila Toth
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

package me.laszloattilatoth.jada;

import me.laszloattilatoth.jada.config.Config;
import me.laszloattilatoth.jada.config.ProxyConfig;
import me.laszloattilatoth.jada.proxy.core.ProxyMain;
import me.laszloattilatoth.jada.proxy.plug.PlugMain;
import me.laszloattilatoth.jada.proxy.socks.SocksMain;
import me.laszloattilatoth.jada.util.Logging;
import org.apache.commons.cli.*;

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

public class Main {
    public static void main(String[] args) {
        Logger logger = Logger.getGlobal();
        System.setProperty("java.util.logging.SimpleFormatter.format", Logging.LOG_FORMAT);
        Logger.getLogger("").setLevel(Level.FINEST);
        Logger.getLogger("").getHandlers()[0].setLevel(Level.FINEST);

        Options options = new Options();
        String filename;

        options.addOption("c", "config", true, "Configuration file");
        options.addOption("V", "validate", true, "Validate only");
        options.addOption(Option.builder("h").longOpt("help").desc("Print help").build());

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            logger.severe("Unable to process arguments; error='" + e.getMessage() + "'");
            System.exit(1);
        }

        if (cmd.hasOption('h') || cmd.hasOption('f')) {
            String header = "A proxy";
            String footer = "\nPoC";

            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("jsocks", header, options, footer, true);
            System.exit(0);
        }

        if (cmd.hasOption('c'))
            filename = cmd.getOptionValue('c');
        else {
            System.err.println("Missing filename");
            System.exit(1);
            return;
        }

        PlugMain.setup();
        SocksMain.setup();

        Config config = null;
        try {
            config = Config.create(filename);
        } catch (FileNotFoundException | Config.InvalidConfig e) {
            Logging.logExceptionWithBacktrace(logger, e, Level.SEVERE);
            System.exit(1);
        }

        if (cmd.hasOption('v'))
            return;

        try {
            proxyLoop(config.proxyConfigs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void proxyLoop(List<ProxyConfig> configs) throws IOException {

        Selector selector = Selector.open();
        List<ProxyMain> proxyMains = new ArrayList<>();

        for (ProxyConfig cfg : configs) {
            ProxyMain m = null;
            switch(cfg.proxyType()) {
                case "socks" -> m = new SocksMain(cfg);
                case "plug" -> m = new PlugMain(cfg);
                default -> throw new IOException();
            }

            proxyMains.add(m);
            m.registerToSelector(selector);
        }

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
