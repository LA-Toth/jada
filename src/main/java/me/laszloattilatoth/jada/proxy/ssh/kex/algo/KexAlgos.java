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

public class KexAlgos {
    private static final Map<Integer, KexAlgo> list = new HashMap<>();

    static {
        put("diffie-hellman-group1-sha1", KexAlgo.Digest.SHA1);
        put("diffie-hellman-group14-sha1", KexAlgo.Digest.SHA1);
    }

    public static KexAlgo byId(int nameId) {
        return list.get(nameId);
    }

    public static KexAlgo byName(String name) {
        for (KexAlgo c : list.values()) {
            if (c.name().equals(name)) {
                return c;
            }
        }

        return null;
    }

    public static KexAlgo byNameWithId(NameWithId nameWithId) {
        return byId(nameWithId.nameId());
    }

    private static void put(String name, KexAlgo.Digest digest) {
        int nameId = Name.getNameId(name);
        list.put(nameId, new KexAlgo(name, nameId, digest));
    }
}
