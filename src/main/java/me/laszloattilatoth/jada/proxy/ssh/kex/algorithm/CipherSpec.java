// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.kex.algorithm;

import me.laszloattilatoth.jada.proxy.ssh.core.Name;

public record CipherSpec(String name, int nameId, int blockSize, int keyLen, int ivLen, int authLen, long flags) {
    // flags are used to map the name to the actual algorithms
    public static int FLAG_CBC = 1;
    public static int FLAG_CHACHAPOLY = 1 << 1; // unused, exists in OpenSSH
    public static int FLAG_AES_CTR = 1 << 2;
    public static int FLAG_3DES = 1 << 3;
    public static int FLAG_AES = 1 << 4;

    public static CipherSpec CIPHER_NONE = new CipherSpec(Name.getName(Name.SSH_NAME_NONE), Name.SSH_NAME_NONE, 8, 0, 0, 0, 0);
}
