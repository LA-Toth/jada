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

package me.laszloattilatoth.jada.proxy.ssh.core;

import java.security.Key;
import java.security.interfaces.DSAKey;
import java.security.interfaces.RSAKey;

public class KeyUtils {
    public static int getKeyTypeId(Key key) {
        if (key == null) {
            return Name.SSH_NAME_UNKNOWN;
        } else if (key instanceof RSAKey) {
            return Name.SSH_NAME_SSH_RSA;
        } else if (key instanceof DSAKey) {
            return Name.SSH_NAME_SSH_DSS;
        } else {
            return Name.SSH_NAME_UNKNOWN;
        }
    }

    public static String getKeyType(Key key) {
        return key == null ? null : Name.getName(getKeyTypeId(key));
    }
}
