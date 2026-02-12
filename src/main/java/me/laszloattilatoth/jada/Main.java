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

package me.laszloattilatoth.jada;

import me.laszloattilatoth.jada.core.AppMain;
import me.laszloattilatoth.jada.util.Logging;
import org.apache.commons.cli.*;
import org.apache.commons.cli.help.HelpFormatter;

import java.io.IOException;
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
        options.addOption("V", "validate", false, "Validate only");
        options.addOption(Option.builder("h").longOpt("help").desc("Print help").get());

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

            HelpFormatter formatter = HelpFormatter.builder().get();
            try {
                formatter.printHelp("jada", header, options, footer, true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.exit(0);
        }

        if (cmd.hasOption('c'))
            filename = cmd.getOptionValue('c');
        else {
            System.err.println("Missing filename. Use the '-h' option for more help");
            System.exit(1);
            return;
        }

        boolean verifyConfig = cmd.hasOption('V');

        new AppMain(filename, verifyConfig, logger).run();
    }
}
