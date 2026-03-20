// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.transportlayer;

import me.laszloattilatoth.jada.proxy.ssh.crypto.CryptoContext;

import java.io.InputStream;

public interface TransportLayerInput {
    void setInputStream(InputStream in) throws TransportLayerException;

    /**
     * Sets (stores) the new keys for upcoming encryption change. The old keys are used till the call
     * of sshMsgNewKeysReceived().
     *
     * @param context
     */
    void addInboundCryptoContext(CryptoContext context);

    void sshMsgNewKeysReceived();

    /**
     * Read packet as RFC 4253, 6.  Binary Packet Protocol
     */
    Packet readPacket() throws TransportLayerException;
}
