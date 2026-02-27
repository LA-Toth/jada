// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.util;

import javax.crypto.KeyAgreement;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;

public class Sec {
    public static Provider provider;

    public static boolean init() {
        provider = new org.bouncycastle.jce.provider.BouncyCastleProvider();
        Security.addProvider(provider);
        try {
            MessageDigest.getInstance("MD5", provider);
            KeyAgreement.getInstance("DH", provider);
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
        return true;
    }
}
