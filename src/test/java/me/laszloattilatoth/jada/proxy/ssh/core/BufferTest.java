package me.laszloattilatoth.jada.proxy.ssh.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BufferTest {
    TestBuffer buffer;

    void assertPosAndLimit(int expectedPos, int expectedLimit) {
        assertEquals(expectedPos, buffer.position(), "position differs");
        assertEquals(expectedLimit, buffer.limit(), "limit differs");
    }

    void assertPosLimitAndCapacity(int expectedPos, int expectedLimit, int expectedCap) {
        assertPosAndLimit(expectedPos, expectedLimit);
        assertEquals(expectedCap, buffer.capacity(), "capacity differs");
    }

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
            assertPosAndLimit(1, 1);
            buffer.putByte(0x11);
            assertPosAndLimit(2, 2);
            buffer.putBytes(new byte[254]);
            assertPosLimitAndCapacity(256, 256, 256);
            buffer.putByte(100);
            assertPosLimitAndCapacity(257, 257, 512);
        } catch (Buffer.BufferEndReachedException e) {
            fail();
        }
    }

    @Test
    public void testBufferWithMaxSizeReallocedBuffer() {
        final int maxSize = 10;
        buffer = new TestBuffer(maxSize);
        assertPosLimitAndCapacity(0, 0, TestBuffer.ALLOC_SIZE);
        assertEquals(TestBuffer.INC_SIZE, buffer.incSize());
        assertEquals(maxSize, buffer.maxSize());

        try {
            buffer.putBytes(new byte[2]);
            buffer.putBytes(new byte[7]);
        } catch (Buffer.BufferEndReachedException e) {
            fail();
        }
        assertPosLimitAndCapacity(9, 9, maxSize);
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
    public class InitiallyNonEmptyBufferTests {

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
            assertPosLimitAndCapacity(10, 10, Buffer.DEFAULT_INIT_SIZE);
        }

        @Test
        public void testFlip() {
            assertPosAndLimit(10, 10);
            assertTrue(buffer.limitReached());

            // as position == limit, the limit is unchanged
            buffer.flip();
            assertPosAndLimit(0, 10);
            assertFalse(buffer.limitReached());

            try {
                assertEquals(0x04, buffer.getByte());
            } catch (Buffer.BufferEndReachedException e) {
                fail();
            }
            assertPosAndLimit(1, 10);

            // resets limit to the current position
            buffer.flip();
            assertPosAndLimit(0, 1);
            assertFalse(buffer.limitReached());

            // clear the buffer
            assertEquals(buffer, buffer.flip());
            assertPosAndLimit(0, 0);
            assertTrue(buffer.limitReached());
        }

        @Test
        public void testResetPosition() {
            assertPosAndLimit(10, 10);
            buffer.resetPosition();
            assertPosAndLimit(0, 10);
            // no change at second call
            buffer.resetPosition();
            assertPosAndLimit(0, 10);
        }

        @Test
        public void testRewind() {
            assertFalse(buffer.isResetPositionCalled);
            buffer.rewind();
            assertTrue(buffer.isResetPositionCalled);
        }

        @Test
        public void testClear() {
            assertPosAndLimit(10, 10);
            buffer.clear();
            assertPosAndLimit(0, 0);
            // no change at second call
            buffer.clear();
            assertPosAndLimit(0, 0);
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
            int limit = buffer.limit();
            assertThrows(Buffer.BufferEndReachedException.class, () -> buffer.getUint64());
            buffer.changeRelativePosition(-7);
            assertThrows(Buffer.BufferEndReachedException.class, () -> buffer.getUint64());
            buffer.changeRelativePosition(-2);
            assertPosAndLimit(1, limit);
            try {
                assertEquals(0x1112132325262728L, buffer.getUint64());
            } catch (Buffer.BufferEndReachedException e) {
                fail();
            }
            assertPosAndLimit(9, limit);
        }
    }
}
