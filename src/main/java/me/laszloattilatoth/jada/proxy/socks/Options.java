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

package me.laszloattilatoth.jada.proxy.socks;

import me.laszloattilatoth.jada.config.ProxyOptions;

import java.util.Map;

public class Options extends ProxyOptions {
    public boolean socksv4 = true;
    public boolean socksv4a = true;
    public boolean socksv5 = true;

    @Override
    public void load(Map<String, Object> globalOptions, Map<String, Object> options) throws InvalidOptions {
        socksv4 = true;
        socksv4a = true;
        socksv5 = true;

        if (globalOptions != null)
            loadSingle(globalOptions);

        if (options != null)
            loadSingle(options);
    }

    private void loadSingle(Map<String, Object> options) throws InvalidOptions {
        for (Map.Entry<String, Object> entry : options.entrySet())
            processEntry(entry);
    }

    private void processEntry(Map.Entry<String, Object> entry) throws InvalidOptions {
        String key = entry.getKey();
        Object val = entry.getValue();
        if (!(val instanceof Boolean))
            throw new InvalidOptions(String.format("Boolean expected for %s", key));
        boolean value = (Boolean) val;
        switch (key) {
            case "socksv4" -> socksv4 = value;
            case "socksv4a" -> socksv4a = value;
            case "socksv5" -> socksv5 = value;
            default -> throw new InvalidOptions(String.format("Unknown SOCKS option  %s", key));
        }
    }
}
