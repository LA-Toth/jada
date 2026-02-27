// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.kex;

import me.laszloattilatoth.jada.proxy.ssh.core.NameWithId;
import me.laszloattilatoth.jada.proxy.ssh.kex.algorithm.CipherSpec;
import me.laszloattilatoth.jada.proxy.ssh.kex.algorithm.CipherRegistry;
import me.laszloattilatoth.jada.proxy.ssh.kex.algorithm.MacSpec;
import me.laszloattilatoth.jada.proxy.ssh.kex.algorithm.MacRegistry;

public class NewKeys {
    public CipherSpec cipherSpec;
    public MacSpec macSpec;

    public void setEncryption(NameWithId encAlg) {
        cipherSpec = CipherRegistry.byNameWithId(encAlg);
    }

    public long cipherKeyLen() {
        return cipherSpec != null ? cipherSpec.keyLen() : 0;
    }

    public long cipherAuthLen() {
        return cipherSpec != null ? cipherSpec.authLen() : 0;
    }

    public void setMac(NameWithId macAlg) {
        macSpec = MacRegistry.byNameWithId(macAlg);
    }

    public void setCompression(NameWithId compAlg) {
        // not supported as of now
    }
}
