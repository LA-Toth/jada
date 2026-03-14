// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.transportlayer;

import me.laszloattilatoth.jada.util.logging.LoggerFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class TransportLayerIOTest {

    @Test
    public void testClearTextReadWithoutBlockSize() {
        TransportLayerInput subject = new TransportLayerIO(LoggerFactory.getNulLogger("test"));
        GrowingInputStream is = new GrowingInputStream();

        try {
            subject.setInputStream(is);
        } catch (TransportLayerException e) {
            fail(e.getMessage());
        }

        is.addInt(2);
        is.addByte((byte) 0);
        is.addByte((byte) 42);

        Packet readPacket;
        try {
            readPacket = subject.readPacket();
        } catch (TransportLayerException e) {
            fail(e.getMessage());
            return; // already failed, make Java happy
        }

        assertEquals(1, readPacket.wpos());
        assertEquals(42, readPacket.packetType());
        try {
            assertEquals(0, is.available(), "not the whole data was read");
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }
}
