package me.laszloattilatoth.jada.proxy.ssh.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BufferTest {

    @Test
    public void testCapacity() {
        Buffer buffer = new Buffer();
        assertEquals(Buffer.DEFAULT_INIT_SIZE, buffer.capacity());
    }
}
