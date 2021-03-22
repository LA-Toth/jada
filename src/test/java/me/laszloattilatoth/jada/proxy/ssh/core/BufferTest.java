package me.laszloattilatoth.jada.proxy.ssh.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BufferTest {

    @Test
    public void testEmptyBuffer() {
        Buffer<TestBuffer> buffer = new TestBuffer();
        assertEquals(Buffer.DEFAULT_INIT_SIZE, buffer.capacity());
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
