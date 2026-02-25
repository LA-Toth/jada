/*
 * Copyright 2021-2026 Laszlo Attila Toth
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

package me.laszloattilatoth.jada.proxy.ssh.kex.dh;

import me.laszloattilatoth.jada.proxy.ssh.kex.KeyExchange;
import me.laszloattilatoth.jada.proxy.ssh.kex.algorithm.KexAlgorithmSpec;
import me.laszloattilatoth.jada.proxy.ssh.kex.dh.mina.BuiltinDHFactories;

import java.nio.charset.StandardCharsets;

public class DHKexFactory {
    public static DHGServer createServer(KeyExchange kex, KexAlgorithmSpec kexAlgorithmSpec, String ownSshIDString, String peerSshIdString, byte[] ownKexInit, byte[] peerKexInit) throws Exception {
        DHGServer server = new DHGServer(kex, BuiltinDHFactories.resolveFactory(kexAlgorithmSpec.name()));
        server.init(
                ownSshIDString.getBytes(StandardCharsets.UTF_8),
                peerSshIdString.getBytes(StandardCharsets.UTF_8),
                ownKexInit,
                peerKexInit
        );

        return server;
    }
}
