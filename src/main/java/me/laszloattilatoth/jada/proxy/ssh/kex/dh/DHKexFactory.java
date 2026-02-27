// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

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
