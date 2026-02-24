/*
 * Copyright 2026 Laszlo Attila Toth
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
