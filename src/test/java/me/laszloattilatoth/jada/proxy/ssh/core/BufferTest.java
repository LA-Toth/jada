package me.laszloattilatoth.jada.proxy.ssh.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class BufferTest {
    static final int LIMIT = 100;

    TestBuffer buffer;

    void assertPosAndLimit(int expectedPos, int expectedLimit) {
        assertEquals(expectedPos, buffer.position(), "position differs");
        assertEquals(expectedLimit, buffer.limit(), "limit differs");
    }

    void assertPosLimitAndCapacity(int expectedPos, int expectedLimit, int expectedCap) {
        assertPosAndLimit(expectedPos, expectedLimit);
        assertEquals(expectedCap, buffer.capacity(), "capacity differs");
    }

    void assertPutMethod(int typeSize, PutMethodExecutable methodToTest, Validator successValidator) {
        beforeEachPutMethodTest();

        try {
            methodToTest.execute();
            assertPosLimitAndCapacity(1 + typeSize, LIMIT, Buffer.DEFAULT_INIT_SIZE);
            successValidator.execute();
        } catch (Buffer.BufferEndReachedException e) {
            fail();
        }

        assertCanWriteAfterInitialSize(typeSize, methodToTest);
        assertCantWriteAfterMaxSize(typeSize, methodToTest::execute);
    }

     void assertCanWriteAfterInitialSize(int typeSize, PutMethodExecutable methodToTest) {
        assertEquals(Buffer.DEFAULT_INIT_SIZE, buffer.initialSize);
        buffer.fillToFirstExpandAtNextByte();
        assertPosLimitAndCapacity(Buffer.DEFAULT_INIT_SIZE, Buffer.DEFAULT_INIT_SIZE, Buffer.DEFAULT_INIT_SIZE);
        buffer.changeRelativePosition(-(typeSize - 1));
        try {
            methodToTest.execute();
        } catch (Buffer.BufferEndReachedException e) {
            fail();
        }
        assertPosLimitAndCapacity(Buffer.DEFAULT_INIT_SIZE + 1, Buffer.DEFAULT_INIT_SIZE + 1, Buffer.DEFAULT_INIT_SIZE + Buffer.DEFAULT_INC_SIZE);
    }

    void assertCantWriteAfterMaxSize(int typeSize, Executable ex) {
        buffer.fillToMaxSize();
        buffer.changeAbsolutePosition(TestBuffer.TEST_MAX_SIZE - (typeSize - 1));
        assertThrows(Buffer.BufferEndReachedException.class, ex, "Should not write after maxSize reached");
    }

    @BeforeEach
    public void beforeEach() {
        buffer = TestBuffer.createAlmostDefaultBuffer();
    }

    public void beforeEachNonEmptyBufferTests() {
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

    void beforeEachPutMethodTest() {
        try {
            for (int i = 0; i != LIMIT; ++i)
                buffer.putByte(0);
            buffer.resetPosition();
            buffer.putByte(1);
        } catch (Buffer.BufferEndReachedException e) {
            fail();
        }
        assertPosLimitAndCapacity(1, LIMIT, Buffer.DEFAULT_INIT_SIZE);
    }

    @Test
    public void testEmptyBuffer() {
        assertEquals(Buffer.DEFAULT_INIT_SIZE, buffer.capacity());
        assertEquals(0, buffer.position());
        assertEquals(0, buffer.limit());
        assertTrue(buffer.limitReached());
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

    @Test
    public void testFlip() {
        beforeEachNonEmptyBufferTests();
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
        beforeEachNonEmptyBufferTests();
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
        beforeEachNonEmptyBufferTests();
        assertPosAndLimit(10, 10);
        buffer.clear();
        assertPosAndLimit(0, 0);
        // no change at second call
        buffer.clear();
        assertPosAndLimit(0, 0);
    }

    @Test
    public void testGetByte() {
        beforeEachNonEmptyBufferTests();
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
    public void testGetBytes() {
        beforeEachNonEmptyBufferTests();
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
        beforeEachNonEmptyBufferTests();
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
        beforeEachNonEmptyBufferTests();
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

    void assertBytes(int startingPosition, byte[] expected) {
        byte[] actual = buffer.array();
        for (int i = 0; i != expected.length; ++i) {
            int finalI = i;
            assertEquals(expected[i], actual[startingPosition + i],
                    () -> String.format("Array should contain %d at pos %d (expected pos %d)", expected[finalI], startingPosition + finalI, finalI));
        }
    }

    @Test
    public void testPutByte() {
        assertPutMethod(1, () -> buffer.putByte(42), () -> assertEquals(42, buffer.array()[1]));
    }

    @Test
    public void testPutBytesWithSingleByte() {
        byte[] data = new byte[]{42};
        assertPutMethod(1, () -> buffer.putBytes(data), () -> assertEquals(42, buffer.array()[1]));
    }

    @Test
    public void testPutBytesWithMultipleBytes() {
        byte[] data = new byte[]{42, 43, 55, 2};
        assertPutMethod(data.length, () -> buffer.putBytes(data), () -> assertBytes(1, data));
    }

    @Test
    public void testPutUint32() {
        byte[] data = new byte[]{0x12, 0x13, 0x14, 0x15};
        assertPutMethod(4, () -> buffer.putUint32(0x12131415), () -> assertBytes(1, data));
    }

    @Test
    public void testPutUint64() {
        byte[] data = new byte[]{0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18};
        assertPutMethod(8, () -> buffer.putUint64(0x1112131415161718L), () -> assertBytes(1, data));
    }

    @Test
    public void testPutBooleanCallsPutByte() {
        try {
            buffer.storedByte = 42;
            buffer.isPutByteCalled = false;
            buffer.putBoolean(true);
            assertEquals(1, buffer.storedByte);
            assertTrue(buffer.isPutByteCalled);

            buffer.storedByte = 42;
            buffer.isPutByteCalled = false;
            buffer.putBoolean(false);
            assertEquals(0, buffer.storedByte);
            assertTrue(buffer.isPutByteCalled);
        } catch (Buffer.BufferEndReachedException e) {
            fail();
        }
    }

    @Test
    public void testPutEmptyString() {
        byte[] data = new byte[]{0x00, 0x00, 0x00, 0x00};
        assertPutMethod(4, () -> buffer.putString(""), () -> assertBytes(1, data));
    }

    @Test
    public void testPutHelloString() {
        byte[] data = new byte[]{0x00, 0x00, 0x00, 0x05, 'h', 'e', 'l', 'l', 'o'};
        assertPutMethod(4 + 5, () -> buffer.putString("hello"), () -> assertBytes(1, data));
    }

    @Test
    public void testPutUnicodeString() {
        // unicodesnowmanforyou.com
        String unicode = "Bőszájú körülíróművész. ☃:";
        byte[] data1 = unicode.getBytes(StandardCharsets.UTF_8);
        byte[] data = new byte[data1.length + 4];
        data[0] = data[1] = data[2] = 0;
        data[3] = (byte) data1.length; // smaller than 256 !
        System.arraycopy(data1, 0, data, 4, data1.length);
        assertPutMethod(data.length, () -> buffer.putString(unicode), () -> assertBytes(1, data));
    }

    interface PutMethodExecutable {
        void execute() throws Buffer.BufferEndReachedException;
    }

    interface Validator {
        void execute();
    }

    private static class TestBuffer extends Buffer<TestBuffer> {
        static final int ALLOC_SIZE = 4;
        static final int INC_SIZE = 8;
        static final int TEST_MAX_SIZE = 1024;

        boolean isPutByteCalled = false;
        boolean isResetPositionCalled = false;
        int storedByte = 4;

        int initialSize;

        TestBuffer() {
            super();
            initialSize = buffer.length;
        }

        TestBuffer(int maxSize) {
            super(ALLOC_SIZE, INC_SIZE, maxSize);
            initialSize = buffer.length;
        }

        TestBuffer(int allocationSize, int incSize, int maxSize) {
            super(allocationSize, incSize, maxSize);
            initialSize = buffer.length;
        }

        static TestBuffer createAlmostDefaultBuffer() {
            return new TestBuffer(Buffer.DEFAULT_INIT_SIZE, Buffer.DEFAULT_INC_SIZE, TEST_MAX_SIZE);
        }

        public TestBuffer resetPosition() {
            TestBuffer b = super.resetPosition();
            isResetPositionCalled = true;
            return b;
        }

        public TestBuffer putByte(int b) throws BufferEndReachedException {
            TestBuffer res = super.putByte(b);
            storedByte = b;
            isPutByteCalled = true;
            return res;
        }

        public void changeAbsolutePosition(int newPos) {
            position = newPos;
            assert position >= 0;
            assert position <= limit;
        }

        public void changeRelativePosition(int amount) {
            changeAbsolutePosition(position + amount);
        }

        void fillToMaxSize() {
            try {
                preserve(TEST_MAX_SIZE - position);
                position = limit = TEST_MAX_SIZE;
            } catch (Buffer.BufferEndReachedException e) {
                fail();
            }
        }

        void fillToFirstExpandAtNextByte() {
            if (capacity() > initialSize)
                return;
            try {
                preserve(initialSize - position);
                position = limit = initialSize;
            } catch (Buffer.BufferEndReachedException e) {
                fail();
            }
        }
    }
}
