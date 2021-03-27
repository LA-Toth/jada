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

import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.util.List;
import java.util.Map;

public class ConfigLoader {
    void load(Config config) throws Config.InvalidConfig {
        if (!config.hasOption("proxies"))
            throw new Config.InvalidConfig();

        Object pr = config.getOption("proxies");
        if (!(pr instanceof List))
            throw new Config.InvalidConfig();

        Object globalOptsObj = config.getOption("global-options");
        if (globalOptsObj != null && !(globalOptsObj instanceof Map))
            throw new Config.InvalidConfig("global-options must be a Map");

        Map<String, Object> globalOptions = (Map<String, Object>) globalOptsObj;
        if (globalOptions != null) {
            for (Map.Entry<String, Object> entry : globalOptions.entrySet()) {
                if (!Config.registeredProxies.containsKey(entry.getKey()))
                    throw new Config.InvalidConfig();
            }
        }

        List<Object> proxies = (List<Object>) pr;
        for (Object o : proxies) {
            if (!(o instanceof Map))
                throw new Config.InvalidConfig();

            Map<String, Object> proxy = (Map<String, Object>) o;
            if (!proxy.containsKey("name") || !(proxy.get("name") instanceof String))
                throw new Config.InvalidConfig();
            if (!proxy.containsKey("proxy") || !(proxy.get("proxy") instanceof String))
                throw new Config.InvalidConfig();
            if (!proxy.containsKey("address") || (!(proxy.get("address") instanceof String) && !(proxy.get("address") instanceof List)))
                throw new Config.InvalidConfig();
            if (!proxy.containsKey("target") || !(proxy.get("target") instanceof String))
                throw new Config.InvalidConfig();
            if (proxy.containsKey("options") && !(proxy.get("options") instanceof Map))
                throw new Config.InvalidConfig();

            SocketAddress[] addresses = loadAddresses(proxy.get("address"));
            SocketAddress target = loadAddressWithInband((String) proxy.get("target"));

            ProxyOptions options = null;
            try {
                options = Config.registeredProxies.get(proxy.get("proxy")).options().getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new Config.InvalidConfig(e.getMessage());
            }

            try {
                options.load((Map<String, Object>) config.getOption("global-options." + proxy.get("proxy")), (Map<String, Object>) proxy.get("options"));
            } catch (ProxyOptions.InvalidOptions invalidOptions) {
                throw new Config.InvalidConfig(invalidOptions.getMessage());
            }
            config.proxyConfigs.add(new ProxyConfig((String) proxy.get("name"), (String) proxy.get("proxy"), addresses, target, options));
        }
    }

    private SocketAddress[] loadAddresses(Object o) throws Config.InvalidConfig {
        SocketAddress[] result = null;
        if (o instanceof String) {
            result = new SocketAddress[1];
            result[0] = loadAddress((String) o);
        } else {
            List<String> sList = (List<String>) o;
            result = new SocketAddress[sList.size()];

            for (int i = 0; i != sList.size(); ++i) {
                result[i] = loadAddress(sList.get(i));
            }
        }

        return result;
    }

    private SocketAddress loadAddress(String addr) throws Config.InvalidConfig {
        boolean ipv4 = false;
        String[] parts = addr.split(":");
        if (addr.startsWith("ip4:") || addr.startsWith("ipv4:")) {
            ipv4 = true;
            if (parts.length != 3)
                throw new Config.InvalidConfig();
            addr = parts[1];
        } else if (addr.startsWith("ip6:") || addr.startsWith("ipv6:")) {
            String[] addrParts = new String[parts.length - 2];
            System.arraycopy(parts, 1, addrParts, 0, addrParts.length);
            addr = String.join(":", addrParts);
        } else {
            ipv4 = true;
            if (parts.length != 2)
                throw new Config.InvalidConfig();
            addr = parts[0];
        }

        InetAddress address = null;
        try {
            if (ipv4)
                address = Inet4Address.getByName(addr);
            else
                address = Inet6Address.getByName(addr);
        } catch (UnknownHostException e) {
            throw new Config.InvalidConfig();
        }

        String portString = parts[parts.length - 1];
        return new InetSocketAddress(address, Short.parseShort(portString));
    }

    private SocketAddress loadAddressWithInband(String addr) throws Config.InvalidConfig {
        if (addr.equals("inband"))
            return null;

        return loadAddress(addr);
    }
}
