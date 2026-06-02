// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.transportlayer.io;

import me.laszloattilatoth.jada.proxy.ssh.crypto.CryptoContext;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.Packet;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.TransportLayerException;

import java.io.InputStream;

public interface TransportLayerInput {
    /**
     * Sets the input stream to read incoming SSH data from.
     *
     * @param in the input stream to read from
     * @throws TransportLayerException if the stream cannot be set or is invalid
     */
    void setInputStream(InputStream in) throws TransportLayerException;

    /**
     * Sets (stores) the new keys for upcoming encryption change. The old keys are used till the call
     * of sshMsgNewKeysReceived().
     *
     * @param context the new crypto context containing the keys and algorithms to be used after key exchange
     */
    void addInboundCryptoContext(CryptoContext context);

    /**
     * Activates the previously added inbound crypto context. Called when an SSH_MSG_NEWKEYS message
     * is received, signaling that the peer has switched to the new keys.
     */
    void sshMsgNewKeysReceived();

    /**
     * Read packet as RFC 4253, 6.  Binary Packet Protocol
     */
    Packet readPacket() throws TransportLayerException;
}
