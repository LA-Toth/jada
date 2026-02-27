// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.kex.algorithm;

public record KexAlgorithmSpec(String name, int nameId, Digest digestId) {
    public enum Digest {
        SHA1,
        SHA256,
        SHA512,
    }
}
