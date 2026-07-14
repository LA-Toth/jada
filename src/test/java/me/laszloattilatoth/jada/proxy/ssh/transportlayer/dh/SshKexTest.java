package me.laszloattilatoth.jada.proxy.ssh.transportlayer.dh;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.*;

public class SshKexTest {

    private static SecureRandom fixedRandom(byte[] seed) throws Exception {
        SecureRandom rnd = SecureRandom.getInstance("SHA1PRNG");
        rnd.setSeed(seed);
        return rnd;
    }

    private static KeyPair fixedRsa(SecureRandom rnd) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048, rnd);
        return kpg.generateKeyPair();
    }

    @Test
    public void testDhGroup14() throws Exception {
        SecureRandom rnd = fixedRandom("dh-test-seed".getBytes(StandardCharsets.UTF_8));
        KeyPair hostKey = fixedRsa(rnd);

        byte[] V_C = "SSH-2.0-testclient".getBytes(StandardCharsets.UTF_8);
        byte[] V_S = "SSH-2.0-testserver".getBytes(StandardCharsets.UTF_8);
        byte[] I_C = new byte[]{1,2,3,4}; // your real KEXINIT payload
        byte[] I_S = new byte[]{5,6,7,8};

        DhWrapper w = new DhWrapper(V_C, V_S, I_C, I_S, hostKey, rnd);
        w.calculate();

        System.out.println("DH e        = " + SshUtil.hex(w.getE().toByteArray()));
        System.out.println("DH f        = " + SshUtil.hex(w.getF().toByteArray()));
        System.out.println("DH K        = " + SshUtil.hex(w.getK().toByteArray()));
        System.out.println("DH H        = " + SshUtil.hex(w.getH()));
        System.out.println("DH sessionId= " + SshUtil.hex(w.getSessionId()));
        DerivedKeys dk = w.getDerivedKeys();
        System.out.println("DH ivC2S    = " + SshUtil.hex(dk.ivC2S()));
        System.out.println("DH ivS2C    = " + SshUtil.hex(dk.ivS2C()));
        System.out.println("DH keyC2S   = " + SshUtil.hex(dk.keyC2S()));
        System.out.println("DH keyS2C   = " + SshUtil.hex(dk.keyS2C()));
        System.out.println("DH macC2S   = " + SshUtil.hex(dk.macC2S()));
        System.out.println("DH macS2C   = " + SshUtil.hex(dk.macS2C()));
        System.out.println("DH reply    = " + SshUtil.hex(w.getReplyAsArray()));

        // once you capture the output, you can turn these into assertEquals(hex, ...)
    }

    @Test
    public void testEcdhNistp256() throws Exception {
        SecureRandom rnd = fixedRandom("ecdh-test-seed".getBytes(StandardCharsets.UTF_8));
        KeyPair hostKey = fixedRsa(rnd);

        byte[] V_C = "SSH-2.0-testclient".getBytes(StandardCharsets.UTF_8);
        byte[] V_S = "SSH-2.0-testserver".getBytes(StandardCharsets.UTF_8);
        byte[] I_C = new byte[]{9,10,11,12};
        byte[] I_S = new byte[]{13,14,15,16};

        EcdhWrapper w = new EcdhWrapper(V_C, V_S, I_C, I_S, hostKey, rnd);
        w.calculate();

        System.out.println("ECDH Q_C    = " + SshUtil.hex(w.getQ_C()));
        System.out.println("ECDH Q_S    = " + SshUtil.hex(w.getQ_S()));
        System.out.println("ECDH K      = " + SshUtil.hex(w.getK().toByteArray()));
        System.out.println("ECDH H      = " + SshUtil.hex(w.getH()));
        System.out.println("ECDH sessionId= " + SshUtil.hex(w.getSessionId()));
        DerivedKeys dk = w.getDerivedKeys();
        System.out.println("ECDH ivC2S  = " + SshUtil.hex(dk.ivC2S()));
        System.out.println("ECDH ivS2C  = " + SshUtil.hex(dk.ivS2C()));
        System.out.println("ECDH keyC2S = " + SshUtil.hex(dk.keyC2S()));
        System.out.println("ECDH keyS2C = " + SshUtil.hex(dk.keyS2C()));
        System.out.println("ECDH macC2S = " + SshUtil.hex(dk.macC2S()));
        System.out.println("ECDH macS2C = " + SshUtil.hex(dk.macS2C()));
        System.out.println("ECDH reply  = " + SshUtil.hex(w.getReplyAsArray()));
    }
}
