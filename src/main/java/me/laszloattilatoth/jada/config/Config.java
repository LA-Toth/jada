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

package me.laszloattilatoth.jada.config;

import me.laszloattilatoth.jada.proxy.core.registration.Registration;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {
    static final Map<String, Registration> registeredProxies = new HashMap<>();
    public final List<ProxyConfig> proxyConfigs = new ArrayList<>();
    private final String filename;
    Map<String, Object> config;

    private Config(String filename) {
        this.filename = filename;
    }

    public static Config create(String filename) throws FileNotFoundException, InvalidConfig {
        Config config = new Config(filename);
        config.load();
        return config;
    }

    public static void registerProxy(Registration registration) {
        registeredProxies.put(registration.name(), registration);
    }

    public static Registration getProxyRegistration(String proxyType) {
        return registeredProxies.get(proxyType);
    }

    public static boolean supportedProxy(String proxyType) {
        return registeredProxies.containsKey(proxyType);
    }

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

    public void load() throws FileNotFoundException, InvalidConfig {
        Yaml yaml = new Yaml();
        config = yaml.load(new FileInputStream(filename));
        new ConfigLoader().load(this);
    }

    public static class InvalidConfig extends Exception {
        InvalidConfig() {
            super();
        }

        InvalidConfig(String message) {
            super(message);
        }
    }
}
