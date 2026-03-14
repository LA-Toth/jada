// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.transportlayer;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

class GrowingInputStream extends InputStream {
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private int readPos = 0;

    public void addBytes(byte[] data) {
        buffer.write(data, 0, data.length);
    }

    public void addByte(byte data) {
        buffer.write(data);
    }

    public void addBytes(byte[] data, int offset, int length) {
        buffer.write(data, offset, length);
    }
    public void addString(String s, Charset charset) {
        byte[] data = s.getBytes(charset);
        addInt(data.length);   // SSH-style length prefix
        addBytes(data);
    }

    public void addInt(int value) {
        // big endian
        buffer.write((value >>> 24) & 0xFF);
        buffer.write((value >>> 16) & 0xFF);
        buffer.write((value >>> 8) & 0xFF);
        buffer.write(value & 0xFF);
    }

    public synchronized void addLong(long value) {
        buffer.write((int) (value >>> 56) & 0xFF);
        buffer.write((int) (value >>> 48) & 0xFF);
        buffer.write((int) (value >>> 40) & 0xFF);
        buffer.write((int) (value >>> 32) & 0xFF);
        buffer.write((int) (value >>> 24) & 0xFF);
        buffer.write((int) (value >>> 16) & 0xFF);
        buffer.write((int) (value >>> 8) & 0xFF);
        buffer.write((int) value & 0xFF);
    }

    @Override
    public int read() {
        if (readPos >= buffer.size()) {
            return -1;
            }
        return buffer.toByteArray()[readPos++] & 0xFF;
    }
}
