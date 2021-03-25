/*
 * Copyright 2020-2021 Laszlo Attila Toth
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

package me.laszloattilatoth.jada.proxy.ssh.kex.algo;

import me.laszloattilatoth.jada.proxy.ssh.core.Name;
import me.laszloattilatoth.jada.proxy.ssh.core.NameWithId;

import java.util.HashMap;
import java.util.Map;

public class Macs {
    private static final Map<Integer, Mac> list = new HashMap<>();

    static {
        put("hmac-sha1");
    }

    public static Mac byId(int nameId) {
        return list.get(nameId);
    }

    public static Mac byName(String name) {
        for (Mac m : list.values()) {
            if (m.name().equals(name)) {
                return m;
            }
        }

        return null;
    }

    public static Mac byNameWithId(NameWithId nameWithId) {
        return byId(nameWithId.nameId());
    }

    private static void put(String name) {
        int nameId = Name.getNameId(name);
        list.put(nameId, new Mac(name, nameId));
    }
}
