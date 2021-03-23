package me.laszloattilatoth.jada.proxy.ssh.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BufferTest {
    TestBuffer buffer;

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
    public void testPutByte() {
        try {
            assertEquals(buffer, buffer.putByte(4));
            assertEquals(1, buffer.position());
            assertEquals(1, buffer.limit());
            buffer.putByte(0x11);
            assertEquals(2, buffer.position());
            assertEquals(2, buffer.limit());
            buffer.putBytes(new byte[254]);
            assertEquals(256, buffer.position());
            assertEquals(256, buffer.limit());
            assertEquals(256, buffer.capacity());
            buffer.putByte(100);
            assertEquals(257, buffer.position());
            assertEquals(257, buffer.limit());
            assertEquals(512, buffer.capacity());
        } catch (Buffer.BufferEndReachedException e) {
            fail();
        }
    }

    @Test
    public void testBufferWithMaxSizeReallocedBuffer() {
        final int maxSize = 10;
        buffer = new TestBuffer(maxSize);
        assertEquals(0, buffer.position());
        assertEquals(0, buffer.limit());
        assertEquals(TestBuffer.ALLOC_SIZE, buffer.capacity());
        assertEquals(TestBuffer.INC_SIZE, buffer.incSize());
        assertEquals(maxSize, buffer.maxSize());

        try {
            buffer.putBytes(new byte[2]);
            buffer.putBytes(new byte[7]);
        } catch (Buffer.BufferEndReachedException e) {
            fail();
        }
        assertEquals(9, buffer.position());
        assertEquals(9, buffer.limit());
        assertEquals(maxSize, buffer.capacity());

        assertThrows(Buffer.BufferEndReachedException.class, () -> buffer.putBytes(new byte[2]));
    }

    private static class TestBuffer extends Buffer<TestBuffer> {
        static final int ALLOC_SIZE = 4;
        static final int INC_SIZE = 8;

        boolean isResetPositionCalled = false;

        TestBuffer() {
            super();
        }

        TestBuffer(int maxSize) {
            super(ALLOC_SIZE, INC_SIZE, maxSize);
        }

        public TestBuffer resetPosition() {
            TestBuffer b = super.resetPosition();
            isResetPositionCalled = true;
            return b;
        }

        public void changeRelativePosition(int amount) {
            position += amount;
            assert position >= 0;
            assert position <= limit;
        }
    }

    @Nested
    public class InitiallyEmptyBufferTests {

        @BeforeEach
        public void beforeEach() {
            try {
                buffer.putByte(4);
                buffer.putByte(0x11);
                buffer.putByte(0x12);
                buffer.putByte(0x13);
                buffer.putByte(0x23);
                buffer.putByte(0x25);
                buffer.putByte(0x26);
                buffer.putByte(0x27);
                buffer.putByte(0x28);
                buffer.putByte(0x29);
            } catch (Buffer.BufferEndReachedException e) {
                fail();
            }
        }

        @Test
        public void testFlip() {
            assertEquals(10, buffer.position());
            assertEquals(10, buffer.limit());
            assertTrue(buffer.limitReached());

            // as position == limit, the limit is unchanged
            buffer.flip();
            assertEquals(0, buffer.position());
            assertEquals(10, buffer.limit());
            assertFalse(buffer.limitReached());

            try {
                assertEquals(0x04, buffer.getByte());
            } catch (Buffer.BufferEndReachedException e) {
                fail();
            }
            assertEquals(1, buffer.position());
            assertEquals(10, buffer.limit());

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

        @Test
        public void testResetPosition() {
            assertEquals(10, buffer.position());
            assertEquals(10, buffer.limit());
            buffer.resetPosition();
            assertEquals(0, buffer.position());
            assertEquals(10, buffer.limit());
            // no change at second call
            buffer.resetPosition();
            assertEquals(0, buffer.position());
            assertEquals(10, buffer.limit());
        }

        @Test
        public void testRewind() {
            assertFalse(buffer.isResetPositionCalled);
            buffer.rewind();
            assertTrue(buffer.isResetPositionCalled);
        }

        @Test
        public void testClear() {
            assertEquals(10, buffer.position());
            assertEquals(10, buffer.limit());
            buffer.clear();
            assertEquals(0, buffer.position());
            assertEquals(0, buffer.limit());
            // no change at second call
            buffer.clear();
            assertEquals(0, buffer.position());
            assertEquals(0, buffer.limit());
        }

        @Test
        public void testReadByte() {
            assertThrows(Buffer.BufferEndReachedException.class, () -> buffer.getByte());
            buffer.flip();
            try {
                assertEquals(0x04, buffer.getByte());
                assertEquals(0x11, buffer.getByte());
            } catch (Buffer.BufferEndReachedException e) {
                fail();
            }
        }

        @Test
        public void testReadBytes() {
            byte[] bytes = null;
            buffer.flip();
            assertThrows(Buffer.BufferEndReachedException.class, () -> buffer.getBytes(11));
            try {
                buffer.getByte();
                bytes = buffer.getBytes(2);
            } catch (Buffer.BufferEndReachedException e) {
                fail();
            }
            assertArrayEquals(new byte[]{0x11, (byte) 0x12}, bytes);
        }

        @Test
        public void testGetUint32Methods() {
            assertThrows(Buffer.BufferEndReachedException.class, () -> buffer.getUint32());
            assertThrows(Buffer.BufferEndReachedException.class, () -> buffer.getUint32AsLong());

            buffer.changeRelativePosition(-3);
            assertThrows(Buffer.BufferEndReachedException.class, () -> buffer.getUint32());
            assertThrows(Buffer.BufferEndReachedException.class, () -> buffer.getUint32AsLong());

            buffer.changeRelativePosition(-1);
            try {
                assertEquals(0x26272829, buffer.getUint32());
                buffer.changeRelativePosition(-4);
                assertEquals(0x26272829, buffer.getUint32AsLong());
            } catch (Buffer.BufferEndReachedException e) {
                fail();
            }
        }

        @Test
        public void testGetUint64() {
            assertThrows(Buffer.BufferEndReachedException.class, () -> buffer.getUint64());
            buffer.changeRelativePosition(-7);
            assertThrows(Buffer.BufferEndReachedException.class, () -> buffer.getUint64());
            buffer.changeRelativePosition(-1);
            try {
                assertEquals(0x1213232526272829L, buffer.getUint64());
            } catch (Buffer.BufferEndReachedException e) {
                fail();
            }
        }
    }
}
