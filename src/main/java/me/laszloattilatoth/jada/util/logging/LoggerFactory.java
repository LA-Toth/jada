// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.util.logging;

import java.util.logging.Handler;
import java.util.logging.Logger;

public class LoggerFactory {
    private LoggerFactory() {
    }

    public static Logger getNulLogger(String name) {
        return getLogger(name, new NullLogHandler());
    }

    public static Logger getLogger(String name, Handler handler) {
        Logger logger = Logger.getLogger(name);
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);
        return logger;
    }
}
