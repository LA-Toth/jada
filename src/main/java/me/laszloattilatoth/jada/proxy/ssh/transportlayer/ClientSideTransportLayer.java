/*
 * Copyright 2020-2021 Laszlo Attila Toth
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

package me.laszloattilatoth.jada.proxy.ssh.transportlayer;

import me.laszloattilatoth.jada.proxy.ssh.SshProxyThread;
import me.laszloattilatoth.jada.proxy.ssh.core.Side;
import me.laszloattilatoth.jada.proxy.ssh.kex.ServerKeyExchange;

import java.nio.channels.SocketChannel;

public class ClientSideTransportLayer extends TransportLayer {
    public ClientSideTransportLayer(SshProxyThread proxy, SocketChannel socketChannel) {
        super(proxy, socketChannel, Side.CLIENT);
        this.kex = new ServerKeyExchange(this);
        this.setupHandlers();
    }
}
