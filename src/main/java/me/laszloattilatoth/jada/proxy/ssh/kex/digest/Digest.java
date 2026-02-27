// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.kex.digest;

import java.security.MessageDigest;

public class Digest {
    private final String name;
    private final MessageDigest md;
    private final int blockSize;

    Digest(String algorithm, int blockSize, MessageDigest md) {
        this.name = algorithm;
        this.blockSize = blockSize;
        this.md = md;
    }

    public String name() {
        return name;
    }

    public int blockSize() {
        return blockSize;
    }

    public void update(byte[] input) {
        md.update(input);
    }

    void update(byte[] input, int offset, int length) {
        md.update(input, offset, length);
    }

    byte[] digest() {
        return md.digest();
    }
}
