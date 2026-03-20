// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.crypto;

import me.laszloattilatoth.jada.proxy.ssh.core.Side;
import me.laszloattilatoth.jada.proxy.ssh.kex.KexOutput;

public record SessionKeys(byte[] iv, byte[] enc_key, byte[] integrity_key) {

    public static SessionKeys createServerSessionKeys(KexOutput output) {
        return new SessionKeys(output.iv_s2c(), output.enc_key_s2c(), output.integrity_key_s2c());
    }

    public static SessionKeys createClientSessionKeys(KexOutput output) {
        return new SessionKeys(output.iv_c2s(), output.enc_key_c2s(), output.integrity_key_c2s());
    }

    public static SessionKeys createOutboundSessionKeys(KexOutput output, Side side) {
        return side.isServer() ? createClientSessionKeys(output) : createServerSessionKeys(output);
    }

    public static SessionKeys createInboundSessionKeys(KexOutput output, Side side) {
        return side.isClient() ? createClientSessionKeys(output) : createServerSessionKeys(output);
    }
}
