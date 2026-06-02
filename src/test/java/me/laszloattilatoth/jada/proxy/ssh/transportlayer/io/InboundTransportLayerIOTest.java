// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.transportlayer.io;

import me.laszloattilatoth.jada.proxy.ssh.transportlayer.GrowingInputStream;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.Packet;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.TransportLayerException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;


/// Testing stream -> read code; see InboundAndOutboundTransportLayerIOTest for testing write & read
public class InboundTransportLayerIOTest extends TransportLayerIOTestBase {

    // positive test case, where the padding is more than 4 bytes long, and ends at the "first" block
    // - the block that contains the 4th byte, which is the second block for a single byte packet
    @Test
    public void testClearTextRead() {
        TransportLayerInput subject = createTransportLayerInput();
        GrowingInputStream is = new GrowingInputStream();

        try {
            subject.setInputStream(is);
        } catch (TransportLayerException e) {
            fail(e.getMessage());
        }

        is.addInt(12);
        is.addByte((byte) 4);
        is.addByte((byte) 42); // payload
        is.addSecureBytes(6); // payload
        is.addSecureBytes(4); // padding

        Packet readPacket;
        try {
            readPacket = subject.readPacket();
        } catch (TransportLayerException e) {
            fail(e.getMessage());
            return; // already failed, make Java happy
        }

        assertEquals(7, readPacket.wpos());
        assertEquals(42, readPacket.packetType());
        try {
            assertEquals(0, is.available(), "not the whole data was read");
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void readClearTextPacketWithoutEndingOnBlockSizeFails() {
        TransportLayerInput subject = createTransportLayerInput();
        GrowingInputStream is = new GrowingInputStream();

        try {
            subject.setInputStream(is);
        } catch (TransportLayerException e) {
            fail(e.getMessage());
        }

        is.addInt(6);
        is.addByte((byte) 4);
        is.addByte((byte) 42);
        is.addSecureBytes(4);

        TransportLayerException exception = assertThrows(
                TransportLayerException.class,
                subject::readPacket,
                "Expected readPacket() to throw, but it didn't"
        );

        assertTrue(exception.getMessage().contains("Read packet size is not multiple of block size"));
    }

    @Test
    public void readClearTextPacketWithTooSmallPaddingFails() {
        TransportLayerInput subject = createTransportLayerInput();
        GrowingInputStream is = new GrowingInputStream();

        try {
            subject.setInputStream(is);
        } catch (TransportLayerException e) {
            fail(e.getMessage());
        }

        is.addInt(12);
        is.addByte((byte) 3);
        is.addByte((byte) 42);
        is.addSecureBytes(7);
        is.addSecureBytes(3);

        TransportLayerException exception = assertThrows(
                TransportLayerException.class,
                subject::readPacket,
                "Expected readPacket() to throw, but it didn't"
        );

        assertTrue(exception.getMessage().contains("Padding length is smaller than"));
    }
}
