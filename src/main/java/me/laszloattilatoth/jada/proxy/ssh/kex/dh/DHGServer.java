/*
 * Modifications of mina-sshd's  org.apache.sshd.server.kex.DHGServer:
 * Copyright 2021-2026 Laszlo Attila Toth
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 *
 * Original copyright:
 * Licensed under the Apache License, Version 2.0 (the "License")
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package me.laszloattilatoth.jada.proxy.ssh.kex.dh;

import me.laszloattilatoth.jada.proxy.ssh.core.Constant;
import me.laszloattilatoth.jada.proxy.ssh.kex.dh.mina.AbstractDHKeyExchange;
import me.laszloattilatoth.jada.proxy.ssh.kex.dh.mina.BuiltinSignatures;
import org.apache.sshd.common.kex.AbstractDH;
import org.apache.sshd.common.kex.DHFactory;
import org.apache.sshd.common.signature.Signature;
import org.apache.sshd.common.util.buffer.Buffer;
import org.apache.sshd.common.util.buffer.ByteArrayBuffer;

import java.security.KeyPair;
import java.util.Objects;
import java.util.logging.Logger;

public class DHGServer extends AbstractDHKeyExchange {

    protected final DHFactory factory;
    protected AbstractDH dh;
    protected final Logger logger;

    public DHGServer(me.laszloattilatoth.jada.proxy.ssh.kex.KeyExchange keyExchange, DHFactory factory) {
        super(keyExchange);
        this.factory = Objects.requireNonNull(factory, "No factory");
        this.logger = keyExchange.transportLayer().getLogger();
    }

    @Override
    public final String getName() {
        return "factory.getName()";
    }

    @Override
    public void init(byte[] v_s, byte[] v_c, byte[] i_s, byte[] i_c) throws Exception {
        super.init(v_s, v_c, i_s, i_c);
        dh = factory.create();
        hash = dh.getHash();
        hash.init();
        setF(dh.getE());
    }

    public void processKexDhInit(Buffer buffer) throws Exception {
        logger.info("Processing DH init packet");
        byte[] e = updateE(buffer);
        dh.setF(e);
        k = dh.getK();

        KeyPair kp = Objects.requireNonNull(kex().getHostKey(), "No server key pair available");

        buffer = new ByteArrayBuffer();
        buffer.putRawPublicKey(kp.getPublic());
        byte[] k_s = buffer.getCompactData();
        byte[] f = getF();

        buffer.clear();
        buffer.putBytes(v_c);
        buffer.putBytes(v_s);
        buffer.putBytes(i_c);
        buffer.putBytes(i_s);
        buffer.putBytes(k_s);
        buffer.putMPInt(e);
        buffer.putMPInt(f);
        buffer.putMPInt(k);

        hash.update(buffer.array(), 0, buffer.wpos());
        h = hash.digest();

        String algo = kex().hostKeyAlgName().name();
        Signature sig = BuiltinSignatures.fromAlgorithmName(algo).create();
        sig.initSigner(null, kp.getPrivate());
        sig.update(null, h);

        buffer.clear();
        buffer.putString(algo);
        byte[] sigBytes = sig.sign(null);
        buffer.putBytes(sigBytes);
        byte[] sigH = buffer.getCompactData();

        buffer.clear();
        buffer.putByte(Constant.SSH_MSG_KEXDH_REPLY);
        buffer.putBytes(k_s);
        buffer.putBytes(f);
        buffer.putBytes(sigH);

        kex().registerNewKeysHandler();

        kex().transportLayer().writePacket(buffer);

        buffer.clear();
        buffer.putByte(Constant.SSH_MSG_NEWKEYS);
        kex().transportLayer().writePacket(buffer);
    }
}
