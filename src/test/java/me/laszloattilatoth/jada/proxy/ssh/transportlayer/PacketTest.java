package me.laszloattilatoth.jada.proxy.ssh.transportlayer;

import me.laszloattilatoth.jada.proxy.ssh.core.Buffer;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

public class PacketTest {

    @Test
    public void testGetLine() {
        byte[] buffer = "first line\r\nsecond line\nthird line\n".getBytes();
        Packet packet = new Packet(buffer);

        assertEquals("first line", packet.getLine());
        assertEquals("second line", packet.getLine());
        assertEquals("third line", packet.getLine());

        buffer = new byte[40];
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        byteBuffer.put("last line\n".getBytes());
        try {
            packet.appendByteBuffer(byteBuffer);
        } catch (Buffer.BufferEndReachedException e) {
            fail();
        }
        assertEquals("last line", packet.getLine());
        assertTrue(packet.limitReached());
    }
}
