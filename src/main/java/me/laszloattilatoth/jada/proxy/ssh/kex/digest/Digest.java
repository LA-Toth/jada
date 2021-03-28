/*
 * Copyright 2021 Laszlo Attila Toth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


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
