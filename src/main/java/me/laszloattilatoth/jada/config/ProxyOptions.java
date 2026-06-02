// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.config;

import java.util.Map;

public class ProxyOptions {
    public void load(Map<String, Object> globalOptions, Map<String, Object> options) throws InvalidOptions {
    }

    /**
     * Convenience helper for subclasses: populate this options instance from a Map using reflection-based loader.
     */
    protected void populateFromMap(Map<String, Object> options) throws InvalidOptions {
        try {
            if (options != null)
                ReflectionConfigLoader.populate(this, options);
        } catch (InvalidOptions e) {
            throw e;
        }
    }
    public static class InvalidOptions extends Exception {
        public InvalidOptions(String s) {
            super(s);
        }
    }
}
