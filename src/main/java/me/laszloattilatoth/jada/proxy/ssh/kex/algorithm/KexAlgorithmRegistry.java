// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.kex.algorithm;

import me.laszloattilatoth.jada.proxy.ssh.core.Name;
import me.laszloattilatoth.jada.proxy.ssh.core.NameWithId;

import java.util.HashMap;
import java.util.Map;

public class KexAlgorithmRegistry {
    private static final Map<Integer, KexAlgorithmSpec> list = new HashMap<>();

    static {
        put("diffie-hellman-group1-sha1", KexAlgorithmSpec.Digest.SHA1);
        put("diffie-hellman-group14-sha1", KexAlgorithmSpec.Digest.SHA1);
        put("diffie-hellman-group14-sha256", KexAlgorithmSpec.Digest.SHA256);
    }

    public static KexAlgorithmSpec byId(int nameId) {
        return list.get(nameId);
    }

    public static KexAlgorithmSpec byName(String name) {
        for (KexAlgorithmSpec c : list.values()) {
            if (c.name().equals(name)) {
                return c;
            }
        }

        return null;
    }

    public static KexAlgorithmSpec byNameWithId(NameWithId nameWithId) {
        return byId(nameWithId.nameId());
    }

    private static void put(String name, KexAlgorithmSpec.Digest digest) {
        int nameId = Name.getNameId(name);
        list.put(nameId, new KexAlgorithmSpec(name, nameId, digest));
    }
}
