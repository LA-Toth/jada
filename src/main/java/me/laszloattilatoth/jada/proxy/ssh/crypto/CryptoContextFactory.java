// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.crypto;

import me.laszloattilatoth.jada.proxy.ssh.core.Direction;

public class CryptoContextFactory {
    CipherFactory cipherFactory = new CipherFactory();

    public CryptoContextFactory() {
    }

    public CryptoContext createContext(CipherSuite suite, SessionKeys sessionKeys, Direction direction) {
        return new CryptoContext(cipherFactory.createCipher(suite.cipherSpec(), sessionKeys.enc_key(), sessionKeys.iv(), direction), null);
    }
}
