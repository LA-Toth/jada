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

import me.laszloattilatoth.jada.util.PathUtils;
import org.apache.sshd.common.config.keys.IdentityUtils;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.util.GenericUtils;
import org.apache.sshd.common.util.io.IoUtils;
import org.apache.sshd.server.config.keys.ServerIdentity;

import java.io.IOException;
import java.nio.file.LinkOption;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

public final class SecurityUtils {
    public static KeyPair loadHostKey(String keyType) {
        try {
            for (KeyPair kp : getServerIdentities()) {
                if (KeyUtils.getKeyType(kp).equals(keyType))
                    return kp;
            }
        } catch (GeneralSecurityException | IOException e) {
            return null;
        }
        return null;
    }

    public static KeyPair loadHostKey() {
        return loadHostKey("ssh-rsa");
    }

    public static Iterable<KeyPair> getServerIdentities() throws GeneralSecurityException, IOException {
        LinkOption[] options = IoUtils.getLinkOptions(true);
        Collection<String> paths = new ArrayList<>();
        paths.add(PathUtils.expandUser("~/.config/jada/ssh_host_rsa_key"));

        Properties props = new Properties();
        props.setProperty(ServerIdentity.HOST_KEY_CONFIG_PROP, GenericUtils.join(paths, ','));

        Map<String, KeyPair> ids = ServerIdentity.loadIdentities(props, options);
        KeyPairProvider provider = IdentityUtils.createKeyPairProvider(ids, true /* supported only */);
        return provider.loadKeys(null);
    }
}
