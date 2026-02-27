// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.kex;

public record KexOutput(byte[] iv_c2s, byte[] iv_s2c, byte[] enc_key_c2s, byte[] enc_key_s2c,
                        byte[] integrity_key_c2s, byte[] integrity_key_s2c) {
}
