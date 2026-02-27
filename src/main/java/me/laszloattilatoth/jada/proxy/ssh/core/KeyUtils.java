// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

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
