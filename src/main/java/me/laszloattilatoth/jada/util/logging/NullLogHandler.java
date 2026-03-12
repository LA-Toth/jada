// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.util.logging;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

public final class NullLogHandler extends Handler {
    @Override
    public void publish(LogRecord record) {
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }
}
