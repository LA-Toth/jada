// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.config;

import me.laszloattilatoth.jada.proxy.core.registration.Registration;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Root runtime configuration loaded from YAML.
 *
 * <p>Stores raw parsed YAML and exposes validated proxy entries as {@link ProxyConfig} values.
 */
public class Config {
    /**
     * Proxy registration metadata keyed by proxy type.
     *
     * <p>Filled via registration APIs before loading configuration.
     */
    static final Map<String, Registration> registeredProxies = new HashMap<>();
    /** Validated proxy configuration entries extracted from the YAML file. */
    public final List<ProxyConfig> proxyConfigs = new ArrayList<>();
    /** Path to the source YAML file. */
    private final String filename;
    /** Raw YAML tree as parsed by SnakeYAML. */
    Map<String, Object> config;

    private Config(String filename) {
        this.filename = filename;
    }

    /**
     * Create and load a configuration object from a YAML file.
     *
     * @param filename YAML file path
     * @return loaded and validated configuration
     * @throws FileNotFoundException when the file cannot be opened
     * @throws InvalidConfig when semantic validation fails
     */
    public static Config create(String filename) throws FileNotFoundException, InvalidConfig {
        Config config = new Config(filename);
        config.load();
        return config;
    }

    /**
     * Register a supported proxy type and its metadata.
     *
     * @param registration registration metadata for one proxy type
     */
    public static void registerProxy(Registration registration) {
        registeredProxies.put(registration.name(), registration);
    }

    /**
     * Resolve registration metadata for a proxy type.
     *
     * @param proxyType proxy type identifier
     * @return registration metadata or {@code null} if not registered
     */
    public static Registration getProxyRegistration(String proxyType) {
        return registeredProxies.get(proxyType);
    }

    /**
     * Check whether a proxy type is registered.
     *
     * @param proxyType proxy type identifier
     * @return {@code true} if supported
     */
    public static boolean supportedProxy(String proxyType) {
        return registeredProxies.containsKey(proxyType);
    }

    /**
     * Check whether a nested YAML option path exists.
     *
     * @param path dot-separated option path (for example {@code global-options.ssh})
     * @return {@code true} when the full path exists
     */
    boolean hasOption(String path) {
        String[] parts = path.split("\\.");

        Object obj = config;

        for (String p :
                parts) {
            if (!(obj instanceof Map))
                return false;

            Map<String, Object> m = (Map<String, Object>) obj;
            if (!m.containsKey(p))
                return false;
            obj = m.get(p);
        }

        return true;
    }

    /**
     * Read a nested YAML option value by dot-separated path.
     *
     * @param path dot-separated option path
     * @return option value or {@code null} if the path is missing or invalid
     */
    Object getOption(String path) {
        String[] parts = path.split("\\.");

        Object obj = config;

        for (String p : parts) {
            if (!(obj instanceof Map))
                return null;

            Map<String, Object> m = (Map<String, Object>) obj;
            if (!m.containsKey(p))
                return null;
            obj = m.get(p);
        }

        return obj;
    }

    /**
     * Parse YAML and validate configuration semantics.
     *
     * @throws FileNotFoundException when the source file cannot be opened
     * @throws InvalidConfig when parsed values fail schema/semantic checks
     */
    public void load() throws FileNotFoundException, InvalidConfig {
        Yaml yaml = new Yaml();
        config = yaml.load(new FileInputStream(filename));
        new ConfigLoader().load(this);
    }

    /** Configuration validation failure. */
    public static class InvalidConfig extends Exception {
        /** Create an exception without message. */
        InvalidConfig() {
            super();
        }

        /**
         * Create an exception with details.
         *
         * @param message validation failure description
         */
        InvalidConfig(String message) {
            super(message);
        }
    }
}
