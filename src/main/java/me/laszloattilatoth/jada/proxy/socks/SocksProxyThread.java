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

import me.laszloattilatoth.jada.config.ProxyConfig;
import me.laszloattilatoth.jada.proxy.core.ProxyThread;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class SocksProxyThread extends ProxyThread {

    public SocksProxyThread(SocketChannel socketChannel, ProxyConfig config) {
        super(socketChannel, config);
    }

    @Override
    protected void runProxy() throws IOException {
        socketChannel.configureBlocking(true);
        int version = socketChannel.socket().getInputStream().read();
        System.out.println(version);

        SocksProxy p = null;
        if (version == -1) {
            return;
        } else if (version == 4 || version == 5) {
            p = SocksProxy.create(version, socketChannel, logger, getName(), threadId);
        } else {
        }

        if (p != null) {
            p.run();
        }
    }
}
