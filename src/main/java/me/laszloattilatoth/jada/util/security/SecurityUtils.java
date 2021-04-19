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

package me.laszloattilatoth.jada.util.security;

import org.apache.sshd.cli.server.SshServerCliSupport;
import org.apache.sshd.common.keyprovider.KeyPairProvider;

import java.io.File;
import java.io.PrintStream;
import java.security.PrivateKey;
import java.util.Collection;

public final class SecurityUtils {
    public static PrivateKey loadPrivateKey(File file) {
        // TODO: support non-RSA keys
        return null;
    }

    public static KeyPairProvider resolveServerKeys(
            PrintStream stderr, String hostKeyType, int hostKeySize, Collection<String> keyFiles)
            throws Exception {
        return SshServerCliSupport.resolveServerKeys(stderr, hostKeyType, hostKeySize, keyFiles);
    }
}
