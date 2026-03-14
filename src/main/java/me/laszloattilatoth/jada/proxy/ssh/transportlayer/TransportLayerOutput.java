// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.transportlayer;

import me.laszloattilatoth.jada.proxy.ssh.kex.NewKeys;
import org.apache.sshd.common.util.buffer.Buffer;

import java.io.IOException;
import java.io.OutputStream;

public interface TransportLayerOutput {
    void setOutputStream(OutputStream out) throws TransportLayerException;

    /**
     * Sets (stores) the new keys for upcoming encryption change. The old keys are used till the call
     * of sshMsgNewKeysReceived().
     *
     * @param newKeys
     */
    void addSenderNewKeys(NewKeys newKeys);

    void sshMsgNewKeysSent();

    void writePacket(Packet packet) throws IOException;

    void writePacket(Buffer packet) throws IOException;

    void writePacket(byte[] bytes, int payloadSize) throws IOException;
}
