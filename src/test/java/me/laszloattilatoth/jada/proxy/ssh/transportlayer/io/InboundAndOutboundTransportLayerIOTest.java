// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.transportlayer.io;

import me.laszloattilatoth.jada.proxy.ssh.core.Direction;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.GrowingInputStream;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.Packet;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.TransportLayerException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;


/// Testing both directions in tests (write packet to stream; read from stream)
public class InboundAndOutboundTransportLayerIOTest extends TransportLayerIOTestBase {
    @Test
    public void writeAndReadClearTextPacketWithOneByte() {
        TransportLayerInput input = createTransportLayerInput();
        TransportLayerOutput output = createTransportLayerOutput();
        GrowingInputStream is = new GrowingInputStream();
        try {
            input.setInputStream(is);
            output.setOutputStream(is.getOutputStream());
        } catch (TransportLayerException e) {
            fail(e.getMessage());
        }

        try {
            Packet packet = new Packet();
            packet.putByte((byte) 42);
            output.writePacket(packet);
        } catch (IOException e) {
            fail(e.getMessage());
        }

        try {
            Packet packet = input.readPacket();
            assertEquals(1, packet.wpos());
            assertEquals(42, packet.packetType());
        } catch (TransportLayerException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void writeAndReadClearTextPacketWithAlmostBlockSizeLength() {
        TransportLayerInput input = createTransportLayerInput();
        TransportLayerOutput output = createTransportLayerOutput();
        GrowingInputStream is = new GrowingInputStream();
        try {
            input.setInputStream(is);
            output.setOutputStream(is.getOutputStream());
        } catch (TransportLayerException e) {
            fail(e.getMessage());
        }

        try {
            Packet packet = new Packet();
            packet.putByte((byte) 42);
            packet.putInt(5);
            packet.putShort((short) 6);
            output.writePacket(packet);
        } catch (IOException e) {
            fail(e.getMessage());
        }

        try {
            Packet packet = input.readPacket();
            assertEquals(7, packet.wpos());
            assertEquals(42, packet.packetType());
            assertEquals(42, packet.getByte());
            assertEquals(0, packet.getByte());
            assertEquals(0, packet.getByte());
            assertEquals(5 * 256 * 256 + 6, packet.getInt());
        } catch (TransportLayerException e) {
            fail(e.getMessage());
        }
    }


    /// Teest the case when the writer is still in cleartext mode
    /// but the reader is prepared for encryption; and the change is not yet happened
    @Test
    public void writeAndReadClearTextPacketWithPreppedReaderEncryption() {
        TransportLayerInput input = createTransportLayerInput();
        TransportLayerOutput output = createTransportLayerOutput();
        GrowingInputStream is = new GrowingInputStream();

        input.addInboundCryptoContext(createCryptoContext(Direction.IN));

        try {
            input.setInputStream(is);
            output.setOutputStream(is.getOutputStream());
        } catch (TransportLayerException e) {
            fail(e.getMessage());
        }

        try {
            Packet packet = new Packet();
            packet.putByte((byte) 42);
            output.writePacket(packet);
        } catch (IOException e) {
            fail(e.getMessage());
        }

        try {
            Packet packet = input.readPacket();
            assertEquals(1, packet.wpos());
            assertEquals(42, packet.packetType());
        } catch (TransportLayerException e) {
            fail(e.getMessage());
        }
    }

    /// Test the case when the reader is still in cleartext mode
    /// but the writer is encrypting
    // @Test
    public void readEncryptedPacketWithCleartextReaderFails() {
        TransportLayerInput input = createTransportLayerInput();
        TransportLayerOutput output = createTransportLayerOutput();
        GrowingInputStream is = new GrowingInputStream();

        input.addInboundCryptoContext(createCryptoContext(Direction.IN));
        output.sshMsgNewKeysSent();

        try {
            input.setInputStream(is);
            output.setOutputStream(is.getOutputStream());
        } catch (TransportLayerException e) {
            fail(e.getMessage());
        }


        try {
            Packet packet = new Packet();
            packet.putByte((byte) 42);
            output.writePacket(packet);
        } catch (IOException e) {
            fail(e.getMessage());
        }

        TransportLayerException exception = assertThrows(
                TransportLayerException.class,
                input::readPacket,
                "Expected readPacket() to throw, but it didn't"
        );


        // TODO: update this
        assertTrue(exception.getMessage().contains("Padding length is smaller than"));
    }

    // @Test
    void writeAndReadEncryptedPacketIsSuccessful() {
        // TODO
    }
}
