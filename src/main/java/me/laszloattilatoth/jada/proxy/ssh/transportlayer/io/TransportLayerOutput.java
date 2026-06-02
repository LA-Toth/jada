// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.transportlayer.io;

import me.laszloattilatoth.jada.proxy.ssh.crypto.CryptoContext;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.Packet;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.TransportLayerException;
import org.apache.sshd.common.util.buffer.Buffer;

import java.io.IOException;
import java.io.OutputStream;

public interface TransportLayerOutput {
    /**
     * Sets the output stream used for writing SSH packets.
     *
     * @param out the output stream to write to
     * @throws TransportLayerException if the output stream cannot be set
     */
    void setOutputStream(OutputStream out) throws TransportLayerException;

    /**
     * Sets (stores) the new keys for upcoming encryption change. The old keys are used till the call
     * of sshMsgNewKeysReceived().
     *
     * @param context the new outbound crypto context to be applied after the next SSH_MSG_NEWKEYS
     */
    void addOutboundCryptoContext(CryptoContext context);

    /**
     * Activates the previously added outbound crypto context after SSH_MSG_NEWKEYS has been sent.
     * From this point on, outbound packets will be encrypted using the new keys.
     */
    void sshMsgNewKeysSent();

    /**
     * Writes an SSH packet to the output stream.
     *
     * @param packet the packet to write
     * @throws IOException if an I/O error occurs
     */
    void writePacket(Packet packet) throws IOException;

    /**
     * Writes an SSH packet from a buffer to the output stream.
     *
     * @param packet the buffer containing the packet data to write
     * @throws IOException if an I/O error occurs
     */
    void writePacket(Buffer packet) throws IOException;

    /**
     * Writes an SSH packet from a raw byte array to the output stream.
     *
     * @param bytes       the byte array containing the packet data
     * @param payloadSize the size of the payload within the byte array
     * @throws IOException if an I/O error occurs
     */
    void writePacket(byte[] bytes, int payloadSize) throws IOException;
}
