// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.crypto;

import me.laszloattilatoth.jada.proxy.ssh.core.Direction;
import me.laszloattilatoth.jada.proxy.ssh.kex.algorithm.CipherSpec;
import me.laszloattilatoth.jada.proxy.ssh.kex.algorithm.KexAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class CipherFactory {
    public Cipher createEncryptingCipher(CipherSpec cipherSpec, byte[] key, byte[] iv) {
        return createCipher(cipherSpec, key, iv, true);
    }

    public Cipher createDecryptingCipher(CipherSpec cipherSpec, byte[] key, byte[] iv) {
        return createCipher(cipherSpec, key, iv, false);
    }

    public Cipher createCipher(CipherSpec cipherSpec, byte[] key, byte[] iv, Direction direction) {
        return createCipher(cipherSpec, key, iv, direction.isOutbound());
    }


    private Cipher createCipher(CipherSpec cipherSpec, byte[] key, byte[] iv, boolean encMode) {
        Cipher cipher;

        try {
            cipher = Cipher.getInstance(cipherSpec.cipherTransformation());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new KexAlgorithmException(e.getMessage());
        }
        try {
            cipher.init(
                    encMode ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE,
                    new SecretKeySpec(key, cipherSpec.secretKeyAlgorithm()),
                    new IvParameterSpec(iv)
            );
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new KexAlgorithmException(e.getMessage());
        }

        return cipher;
    }

}
