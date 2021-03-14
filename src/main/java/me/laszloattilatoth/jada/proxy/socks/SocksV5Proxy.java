/*
 * Copyright 2021 Laszlo Attila Toth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.laszloattilatoth.jada.proxy.socks;

import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

public class SocksV5Proxy extends SocksProxy {
    private InetAddress address = null;
    private short port = 0;

    SocksV5Proxy(SocketChannel s, Logger logger, String name, int threadId) {
        super(s, logger, name, threadId);
    }

    @Override
    public void run() throws IOException {
        InputStream inputStream = socketChannel.socket().getInputStream();
        if (!processAuthentication(inputStream) || !processAddress(inputStream))
            return;

        connectAndTransfer(address, port);
    }

    private boolean processAuthentication(InputStream inputStream) throws IOException {
        int authCnt = inputStream.read();
        if (authCnt < 0)
            return false;

        if (authCnt > 0) {
            byte[] buffer = new byte[authCnt];
            if (inputStream.read(buffer) != authCnt)
                return false;

            boolean hasNoAuth = false;
            for (byte b : buffer) {
                if (b == 0) {
                    hasNoAuth = true;
                    break;
                }
            }
            if (!hasNoAuth) {
                socketChannel.write(ByteBuffer.wrap(new byte[]{0x05, (byte) 0xff}));
                return false;
            }
        }

        socketChannel.write(ByteBuffer.wrap(new byte[]{0x05, (byte) 0x00}));
        return true;
    }

    private boolean processAddress(InputStream inputStream) throws IOException {
        if (inputStream.read() != 0x05 || inputStream.read() != 0x01 || inputStream.read() != 0x00)
            return false;

        int t = inputStream.read();
        if (t != 0x1 && t != 0x03 && t != 0x04)
            return false;

        if (t == 0x01) {
            byte[] b = new byte[4];
            if (inputStream.read(b) != 4)
                return false;

            address = InetAddress.getByAddress(b);
        } else if (t == 0x03) {
            int b;
            StringBuilder buffer = new StringBuilder();
            while ((b = inputStream.read()) > 0) {
                buffer.append((char) b);
            }
            if (b == -1) {
                return false;
            }

            address = InetAddress.getByName(buffer.toString());
        } else {
            byte[] b = new byte[16];
            if (inputStream.read(b) != 16)
                return false;

            address = InetAddress.getByAddress(b);
        }

        byte[] portBytes = new byte[2];
        if (inputStream.read(portBytes) != 2)
            return false;
        port = ByteBuffer.wrap(portBytes).getShort();
        return true;
    }

    @Override
    protected void sendConnectionSuccessMsg() throws IOException {
        sendResponse(0x00);
    }

    @Override
    protected void sendConnectionFailureMsg() throws IOException {
        sendResponse(0x04);
    }

    private void sendResponse(int response) throws IOException {
        socketChannel.socket().getOutputStream().write(new byte[]{0x05, (byte) response, 0x00}, 0, 3);

        boolean ipv4 = (address instanceof Inet4Address);

        socketChannel.socket().getOutputStream().write(ipv4 ? 0x01 : 0x04);
        byte[] b = address.getAddress();
        socketChannel.socket().getOutputStream().write(b);
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.putShort(port);
        socketChannel.socket().getOutputStream().write(buffer.array());
    }
}
