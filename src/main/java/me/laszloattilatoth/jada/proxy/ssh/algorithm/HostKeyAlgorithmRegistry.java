// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

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
