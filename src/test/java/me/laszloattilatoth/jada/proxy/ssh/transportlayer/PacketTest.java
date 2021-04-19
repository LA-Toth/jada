package me.laszloattilatoth.jada.proxy.ssh.transportlayer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PacketTest {

    @Test
    public void testGetLine() {
        byte[] buffer = "first line\r\nsecond line\nthird line\n".getBytes();
        Packet packet = new Packet(buffer);

        assertEquals("first line", packet.getLine());
        assertEquals("second line", packet.getLine());
        assertEquals("third line", packet.getLine());
        assertTrue(packet.endReached());

        packet.putRawBytes("last line\n".getBytes());
        assertEquals("last line", packet.getLine());
        assertTrue(packet.endReached());
    }
}
