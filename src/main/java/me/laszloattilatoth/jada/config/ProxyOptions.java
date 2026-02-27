// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.config;

import java.util.Map;

public class ProxyOptions {
    public void load(Map<String, Object> globalOptions, Map<String, Object> options) throws InvalidOptions {}

    public static class InvalidOptions extends Exception {
        public InvalidOptions(String s) {
            super(s);
        }
    }
}
