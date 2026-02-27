// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.kex.digest;

import org.bouncycastle.jcajce.provider.digest.SHA1;
import org.bouncycastle.jcajce.provider.digest.SHA256;
import org.bouncycastle.jcajce.provider.digest.SHA512;

public class DigestFactory {
    public static Digest createSHA1() {
        return new Digest("SHA1", 20, new SHA1.Digest());
    }

    public static Digest createSHA256() {
        return new Digest("SHA256", 20, new SHA256.Digest());
    }

    public static Digest createSHA512() {
        return new Digest("SHA512", 20, new SHA512.Digest());
    }
}
