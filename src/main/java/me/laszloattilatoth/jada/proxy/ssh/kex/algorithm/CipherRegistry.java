// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.kex.algorithm;

import me.laszloattilatoth.jada.proxy.ssh.core.Name;
import me.laszloattilatoth.jada.proxy.ssh.core.NameWithId;

import java.util.HashMap;
import java.util.Map;

public class CipherRegistry {
    private static final Map<Integer, CipherSpec> list = new HashMap<>();

    static {
        put("3des-cbc", 8, 24, 0, 0, CipherSpec.FLAG_3DES | CipherSpec.FLAG_CBC);
        put("aes128-cbc", 16, 16, 0, 0, CipherSpec.FLAG_AES | CipherSpec.FLAG_CBC);
        put("aes192-cbc", 16, 24, 0, 0, CipherSpec.FLAG_AES | CipherSpec.FLAG_CBC);
        put("aes256-cbc", 16, 32, 0, 0, CipherSpec.FLAG_AES | CipherSpec.FLAG_CBC);
        put("aes128-ctr", 16, 16, 0, 0, CipherSpec.FLAG_AES | CipherSpec.FLAG_AES_CTR);
        put("aes192-ctr", 16, 24, 0, 0, CipherSpec.FLAG_AES | CipherSpec.FLAG_AES_CTR);
        put("aes256-ctr", 16, 32, 0, 0, CipherSpec.FLAG_AES | CipherSpec.FLAG_AES_CTR);
    }

    public static CipherSpec byId(int nameId) {
        return list.get(nameId);
    }

    public static CipherSpec byName(String name) {
        for (CipherSpec c : list.values()) {
            if (c.name().equals(name)) {
                return c;
            }
        }

        return null;
    }

    public static CipherSpec byNameWithId(NameWithId nameWithId) {
        return byId(nameWithId.nameId());
    }

    private static void put(String name, long blockSize, long keyLen, long ivLen, long authLen, long flags) {
        int nameId = Name.getNameId(name);
        list.put(nameId, new CipherSpec(name, nameId, blockSize, keyLen, ivLen != 0 ? ivLen : blockSize, authLen, flags));
    }
}
