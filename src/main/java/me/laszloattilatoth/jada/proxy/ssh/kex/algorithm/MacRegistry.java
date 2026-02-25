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

package me.laszloattilatoth.jada.proxy.ssh.kex.algorithm;

import me.laszloattilatoth.jada.proxy.ssh.core.Name;
import me.laszloattilatoth.jada.proxy.ssh.core.NameWithId;

import java.util.HashMap;
import java.util.Map;

public class MacRegistry {
    private static final Map<Integer, MacSpec> list = new HashMap<>();

    static {
        put("hmac-md5", 0, 16, 16);   // full 128-bit MD5
        put("hmac-md5-96", 96, 16, 12);   // truncated to 96 bits

        put("hmac-sha1", 0, 20, 20);   // full SHA-1
        put("hmac-sha1-96", 96, 20, 12);   // truncated to 96 bits

        put("hmac-sha2-256", 0, 32, 32);   // SHA-256
        put("hmac-sha2-512", 0, 64, 64);   // SHA-512
    }

    public static MacSpec byId(int nameId) {
        return list.get(nameId);
    }

    public static MacSpec byName(String name) {
        for (MacSpec m : list.values()) {
            if (m.name().equals(name)) {
                return m;
            }
        }

        return null;
    }

    public static MacSpec byNameWithId(NameWithId nameWithId) {
        return byId(nameWithId.nameId());
    }

    private static void put(String name, int truncateBits, int keyLen, int len) {
        int nameId = Name.getNameId(name);
        list.put(nameId, new MacSpec(name, nameId, truncateBits, keyLen, len));
    }
}
