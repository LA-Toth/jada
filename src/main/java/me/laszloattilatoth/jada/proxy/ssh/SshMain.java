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

package me.laszloattilatoth.jada.proxy.ssh;

import me.laszloattilatoth.jada.config.ProxyConfig;
import me.laszloattilatoth.jada.config.ProxyOptions;
import me.laszloattilatoth.jada.proxy.core.ProxyMain;
import me.laszloattilatoth.jada.proxy.core.ProxyThread;
import me.laszloattilatoth.jada.proxy.core.registration.Registrar;

import java.nio.channels.SocketChannel;

public class SshMain extends ProxyMain {
    public SshMain(ProxyConfig config) {
        super(config);
    }

    public static void setup() {
        Registrar.registerProxy("ssh", SshMain.class, ProxyOptions.class);
    }

    @Override
    public void start(SocketChannel channel) {
        ProxyThread t = new SshProxyThread(channel, config, nextThreadId());
        t.start();
    }
}
