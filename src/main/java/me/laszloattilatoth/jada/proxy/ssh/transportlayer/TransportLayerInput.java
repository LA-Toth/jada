// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.transportlayer;

import me.laszloattilatoth.jada.proxy.ssh.kex.NewKeys;

import java.io.InputStream;

public interface TransportLayerInput {
    void setInputStream(InputStream in) throws TransportLayerException;

    /**
     * Sets (stores) the new keys for upcoming encryption change. The old keys are used till the call
     * of sshMsgNewKeysReceived().
     *
     * @param newKeys
     */
    void addReceiverNewKeys(NewKeys newKeys);

    void sshMsgNewKeysReceived();

    /**
     * Read packet as RFC 4253, 6.  Binary Packet Protocol
     */
    Packet readPacket() throws TransportLayerException;
}
