package me.laszloattilatoth.jada.proxy.ssh.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BufferTest {
    Buffer<TestBuffer> buffer;

    @BeforeEach
    public void beforeEach() {
        buffer = new TestBuffer();
    }

    @Test
    public void testEmptyBuffer() {
        assertEquals(Buffer.DEFAULT_INIT_SIZE, buffer.capacity());
        assertEquals(0, buffer.position());
        assertEquals(0, buffer.limit());
        assertTrue(buffer.limitReached());
    }

    @Test
    public void testFlip() {
        try {
            buffer.putByte(4);
            buffer.putByte(0xaa);
        } catch (Buffer.BufferEndReachedException e) {
            fail();
        }
        assertEquals(2, buffer.position());
        assertEquals(2, buffer.limit());
        assertTrue(buffer.limitReached());

        // as position == limit, the limit is unchanged
        buffer.flip();
        assertEquals(0, buffer.position());
        assertEquals(2, buffer.limit());
        assertFalse(buffer.limitReached());

        try {
            assertEquals(0x04, buffer.getByte());
        } catch (Buffer.BufferEndReachedException e) {
            fail();
        }
        assertEquals(1, buffer.position());
        assertEquals(2, buffer.limit());

        // resets limit to the current position
        buffer.flip();
        assertEquals(0, buffer.position());
        assertEquals(1, buffer.limit());
        assertFalse(buffer.limitReached());

        // clear the buffer
        assertEquals(buffer, buffer.flip());
        assertEquals(0, buffer.position());
        assertEquals(0, buffer.limit());
        assertTrue(buffer.limitReached());
    }

    private static class TestBuffer extends Buffer<TestBuffer> {
        TestBuffer() {
            super();
        }
    }
}
