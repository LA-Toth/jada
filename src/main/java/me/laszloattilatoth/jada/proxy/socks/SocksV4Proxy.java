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
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

public class SocksV4Proxy extends SocksProxy {
    private final byte[] portBytes = new byte[2];
    private final byte[] addrBytes = new byte[4];
    private boolean socksV4A = false;

    SocksV4Proxy(SocketChannel s, Logger logger, String name, int threadId) {
        super(s, logger, name, threadId);
    }

    @Override
    public void run() throws IOException {
        InputStream inputStream = socketChannel.socket().getInputStream();
        int cmd = inputStream.read();
        if (cmd != 1)
            return;

        if (inputStream.read(portBytes) <= 0 || inputStream.read(addrBytes) <= 0 || inputStream.read() < 0)
            return;

        socksV4A = addrBytes[0] == 0 && addrBytes[1] == 0 && addrBytes[2] == 0 && addrBytes[3] != 0;

        short port = ByteBuffer.wrap(portBytes).getShort();
        InetAddress address = null;
        if (socksV4A) {
            int b;
            StringBuilder buffer = new StringBuilder();
            while ((b = inputStream.read()) > 0) {
                buffer.append((char) b);
            }
            if (b == -1) {
                // SOCKS4a client issue, pointless to send anything
                return;
            }

            address = InetAddress.getByName(buffer.toString());
            logger.info(() -> String.format("Receiving SOCKS4a request; address='%s'", buffer.toString()));
        } else {
            address = InetAddress.getByAddress(addrBytes);
        }

        connectAndTransfer(address, port);
    }

    @Override
    protected void sendConnectionSuccessMsg() throws IOException {
        socketChannel.socket().getOutputStream().write(new byte[]{0, 0x5A}, 0, 2);
        socketChannel.socket().getOutputStream().write(portBytes);
        socketChannel.socket().getOutputStream().write(addrBytes);
    }

    @Override
    protected void sendConnectionFailureMsg() throws IOException {
        socketChannel.socket().getOutputStream().write(new byte[]{0, 0x5B}, 0, 2);
        socketChannel.socket().getOutputStream().write(portBytes);
        socketChannel.socket().getOutputStream().write(addrBytes);
    }
}
