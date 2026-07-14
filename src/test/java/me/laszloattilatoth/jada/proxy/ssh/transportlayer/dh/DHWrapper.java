package me.laszloattilatoth.jada.proxy.ssh.transportlayer.dh;

import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Signature;

final class DhWrapper {

    private final byte[] V_C;
    private final byte[] V_S;
    private final byte[] I_C;
    private final byte[] I_S;
    private final KeyPair hostKey;
    private final SecureRandom rnd;

    // exposed intermediates
    private BigInteger e;
    private BigInteger f;
    private BigInteger K;
    private byte[] H;
    private byte[] sessionId;
    private DerivedKeys derived;
    private byte[] kexdhReply;

    DhWrapper(byte[] clientIdent,
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
        // RFC 4253 group14
        BigInteger p = new BigInteger(
                "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1" +
                        "29024E088A67CC74020BBEA63B139B22514A08798E3404DD" +
                        "EF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245" +
                        "E485B576625E7EC6F44C42E9A63A36210000000000090563", 16);
        BigInteger g = BigInteger.valueOf(2);

        DHParameterSpec spec = new DHParameterSpec(p, g);
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DH");
        kpg.initialize(spec, rnd);

        // client ephemeral
        KeyPair clientDh = kpg.generateKeyPair();
        e = ((DHPublicKey) clientDh.getPublic()).getY();

        // server ephemeral
        KeyPair serverDh = kpg.generateKeyPair();
        f = ((DHPublicKey) serverDh.getPublic()).getY();

        // shared secret K (server side)
        BigInteger x_s = ((DHPrivateKey) serverDh.getPrivate()).getX();
        K = e.modPow(x_s, p);

        // exchange hash H (RFC 4253 §8)
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        SshUtil.writeString(buf, V_C);
        SshUtil.writeString(buf, V_S);
        SshUtil.writeString(buf, I_C);
        SshUtil.writeString(buf, I_S);
        SshUtil.writeString(buf, hostKey.getPublic().getEncoded());
        SshUtil.writeMpint(buf, e);
        SshUtil.writeMpint(buf, f);
        SshUtil.writeMpint(buf, K);
        H = SshUtil.sha1(buf.toByteArray());

        sessionId = H; // first KEX

        // derive keys (RFC 4253 §7.2, SHA‑1)
        derived = deriveKeys(K, H, sessionId);

        // build SSH_MSG_KEXDH_REPLY (no padding)
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(31); // SSH_MSG_KEXDH_REPLY
        SshUtil.writeString(out, hostKey.getPublic().getEncoded());
        SshUtil.writeMpint(out, f);

        Signature sig = Signature.getInstance("SHA1withRSA");
        sig.initSign(hostKey.getPrivate(), rnd);
        sig.update(H);
        byte[] signature = sig.sign();
        SshUtil.writeString(out, signature);

        kexdhReply = out.toByteArray();
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
        return SshUtil.sha1(buf.toByteArray());
    }

    // getters for comparison

    public BigInteger getE() { return e; }
    public BigInteger getF() { return f; }
    public BigInteger getK() { return K; }
    public byte[] getH() { return H; }
    public byte[] getSessionId() { return sessionId; }
    public DerivedKeys getDerivedKeys() { return derived; }
    public byte[] getReplyAsArray() { return kexdhReply; }
}
