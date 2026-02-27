// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.core;

import java.security.SecureRandom;

public class SecureRandomWithByteArray {
    private static final SecureRandom secureRandom = new SecureRandom();

    private final byte[] bytes;

    public SecureRandomWithByteArray(int size) {
        bytes = new byte[size];
    }

    public SecureRandom getSecureRandom() {
        return secureRandom;
    }

    public byte[] getSecureBytes() {
        secureRandom.nextBytes(bytes);
        return bytes;
    }
}
