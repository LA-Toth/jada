/*
 * Copyright 2020-2026 Laszlo Attila Toth
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

package me.laszloattilatoth.jada.proxy.ssh.algorithm;

import me.laszloattilatoth.jada.proxy.ssh.core.Name;
import me.laszloattilatoth.jada.proxy.ssh.core.NameWithId;

import java.util.HashMap;
import java.util.Map;

public class HostKeyAlgorithmRegistry {
    private static final Map<Integer, HostKeyAlgorithmSpec> list = new HashMap<>();

    static {
        put("ssh-rsa");
        put("ssh-dss");
    }

    public static HostKeyAlgorithmSpec byId(int nameId) {
        return list.get(nameId);
    }

    public static HostKeyAlgorithmSpec byName(String name) {
        for (HostKeyAlgorithmSpec c : list.values()) {
            if (c.name().equals(name)) {
                return c;
            }
        }

        return null;
    }

    public static HostKeyAlgorithmSpec byNameWithId(NameWithId nameWithId) {
        return byId(nameWithId.nameId());
    }

    private static void put(String name) {
        int nameId = Name.getNameId(name);
        list.put(nameId, new HostKeyAlgorithmSpec(name, nameId));
    }
}
