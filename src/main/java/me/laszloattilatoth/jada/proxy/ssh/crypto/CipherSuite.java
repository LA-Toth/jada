// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.crypto;

import me.laszloattilatoth.jada.proxy.ssh.kex.algorithm.CipherSpec;
import me.laszloattilatoth.jada.proxy.ssh.kex.algorithm.MacSpec;

public class CipherSuite {
    private CipherSpec cipherSpec;
    private MacSpec macSpec;

    public int cipherBlockSize() {
        return cipherSpec == null ? 8 : cipherSpec.blockSize();
    }

    public CipherSpec cipherSpec() {
        return cipherSpec;
    }

    public void setCipherSpec(CipherSpec cipherSpec) {
        this.cipherSpec = cipherSpec;
    }

    public MacSpec macSpec() {
        return macSpec;
    }

    public void setMacSpec(MacSpec macSpec) {
        this.macSpec = macSpec;
    }
}
