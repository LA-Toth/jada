package me.laszloattilatoth.jada.proxy.ssh.transportlayer.dh;

import javax.crypto.KeyAgreement;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;

final class EcdhWrapper {

    private final byte[] V_C;
    private final byte[] V_S;
    private final byte[] I_C;
    private final byte[] I_S;
    private final KeyPair hostKey;
    private final SecureRandom rnd;

    private byte[] Q_C;
    private byte[] Q_S;
    private BigInteger K;
    private byte[] H;
    private byte[] sessionId;
    private DerivedKeys derived;
    private byte[] kexEcdhReply;

    EcdhWrapper(byte[] clientIdent,
                byte[] serverIdent,
                byte[] clientKexInit,
                byte[] serverKexInit,
                KeyPair hostKey,
                SecureRandom rnd) {
        this.V_C = clientIdent;
        this.V_S = serverIdent;
        this.I_C = clientKexInit;
        this.I_S = serverKexInit;
        this.hostKey = hostKey;
        this.rnd = rnd;
    }

    public void calculate() throws Exception {
        // ecdh-sha2-nistp256 (RFC 5656)
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(new ECGenParameterSpec("secp256r1"), rnd);

        KeyPair clientEc = kpg.generateKeyPair();
        KeyPair serverEc = kpg.generateKeyPair();

        ECPublicKey clientPub = (ECPublicKey) clientEc.getPublic();
        ECPublicKey serverPub = (ECPublicKey) serverEc.getPublic();

        Q_C = clientPub.getEncoded();
        Q_S = serverPub.getEncoded();

        // shared secret K (server side)
        KeyAgreement ka = KeyAgreement.getInstance("ECDH");
        ka.init(serverEc.getPrivate(), rnd);
        ka.doPhase(clientPub, true);
        byte[] secret = ka.generateSecret();
        // SSH treats K as mpint of big-endian integer
        K = new BigInteger(1, secret);

        // exchange hash H (RFC 5656 §4)
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        SshUtil.writeString(buf, V_C);
        SshUtil.writeString(buf, V_S);
        SshUtil.writeString(buf, I_C);
        SshUtil.writeString(buf, I_S);
        SshUtil.writeString(buf, hostKey.getPublic().getEncoded());
        SshUtil.writeString(buf, Q_C);
        SshUtil.writeString(buf, Q_S);
        SshUtil.writeMpint(buf, K);
        H = SshUtil.sha256(buf.toByteArray());

        sessionId = H;

        // derive keys (same structure, SHA‑256)
        derived = deriveKeys(K, H, sessionId);

        // SSH_MSG_KEX_ECDH_REPLY (RFC 5656 §4)
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(31); // same message number as KEXDH_REPLY
        SshUtil.writeString(out, hostKey.getPublic().getEncoded());
        SshUtil.writeString(out, Q_S);

        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(hostKey.getPrivate(), rnd);
        sig.update(H);
        byte[] signature = sig.sign();
        SshUtil.writeString(out, signature);

        kexEcdhReply = out.toByteArray();
    }

    private static DerivedKeys deriveKeys(BigInteger K, byte[] H, byte[] sessionId) throws Exception {
        return new DerivedKeys(
                derive(K, H, sessionId, 'A'),
                derive(K, H, sessionId, 'B'),
                derive(K, H, sessionId, 'C'),
                derive(K, H, sessionId, 'D'),
                derive(K, H, sessionId, 'E'),
                derive(K, H, sessionId, 'F')
        );
    }

    private static byte[] derive(BigInteger K, byte[] H, byte[] sessionId, char letter) throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        SshUtil.writeMpint(buf, K);
        buf.write(H);
        buf.write((byte) letter);
        buf.write(sessionId);
        return SshUtil.sha256(buf.toByteArray());
    }

    // getters

    public byte[] getQ_C() { return Q_C; }
    public byte[] getQ_S() { return Q_S; }
    public BigInteger getK() { return K; }
    public byte[] getH() { return H; }
    public byte[] getSessionId() { return sessionId; }
    public DerivedKeys getDerivedKeys() { return derived; }
    public byte[] getReplyAsArray() { return kexEcdhReply; }
}
