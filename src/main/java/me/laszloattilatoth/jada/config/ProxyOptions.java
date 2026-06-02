// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.config;

import java.util.Map;

/**
 * Base type for proxy-specific option models.
 *
 * <p>Concrete proxy implementations can subclass this and override {@link #load(Map, Map)}
 * to merge global and per-proxy options.
 */
public class ProxyOptions {
    /**
     * Load proxy options from global and local maps.
     *
     * <p>The base implementation is intentionally empty.
     *
     * @param globalOptions global options map for a proxy type (may be {@code null})
     * @param options per-proxy options map (may be {@code null})
     * @throws InvalidOptions when values are invalid
     */
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

    /** Proxy options validation failure. */
    public static class InvalidOptions extends Exception {
        /**
         * Create an options-validation exception.
         *
         * @param s failure description
         */
        public InvalidOptions(String s) {
            super(s);
        }
    }
}
