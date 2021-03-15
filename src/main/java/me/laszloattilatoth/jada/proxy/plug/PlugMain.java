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

package me.laszloattilatoth.jada.proxy.plug;

import me.laszloattilatoth.jada.config.Config;
import me.laszloattilatoth.jada.config.ProxyConfig;
import me.laszloattilatoth.jada.config.ProxyOptions;
import me.laszloattilatoth.jada.proxy.core.ProxyMain;
import me.laszloattilatoth.jada.proxy.core.ProxyThread;

import java.nio.channels.SocketChannel;

public class PlugMain extends ProxyMain {
    public PlugMain(ProxyConfig config) {
        super(config);
    }

    public static void setup() {
        Config.registerProxy("plug", ProxyOptions.class);
    }

    @Override
    public void start(SocketChannel channel) {
        ProxyThread t = new PlugProxyThread(channel, config);
        t.start();
    }
}
