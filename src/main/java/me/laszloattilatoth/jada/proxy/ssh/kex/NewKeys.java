/*
 * Copyright 2020-2026 Laszlo Attila Toth
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
