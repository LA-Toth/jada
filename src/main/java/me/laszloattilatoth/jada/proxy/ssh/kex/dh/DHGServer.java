// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

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
    protected final Logger logger;
    protected AbstractDH dh;

    public DHGServer(me.laszloattilatoth.jada.proxy.ssh.kex.KeyExchange keyExchange, DHFactory factory) {
        super(keyExchange);
        this.factory = Objects.requireNonNull(factory, "No factory");
        this.logger = keyExchange.transportLayer().logger();
    }

    @Override
    public final String getName() {
        return "factory.getName()";
    }

    /**
     * @param v_s Server ID string
     * @param v_c Client ID string
     * @param i_s Server SSH_MSG_KEX_INIT packet
     * @param i_c Client SSH_MSG_KEX_INIT packet
     */
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
