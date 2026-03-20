// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.crypto;

import javax.crypto.Cipher;
import javax.crypto.Mac;

public record CryptoContext(Cipher cipher, Mac mac) {

}
