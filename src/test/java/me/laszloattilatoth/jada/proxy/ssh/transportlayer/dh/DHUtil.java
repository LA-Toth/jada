package me.laszloattilatoth.jada.proxy.ssh.transportlayer.dh;

import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

public final class DHUtil {

    public static KeyPair generateKeyPair(BigInteger p, BigInteger g) throws Exception {
        DHParameterSpec spec = new DHParameterSpec(p, g);
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DH");
        kpg.initialize(spec);
        return kpg.generateKeyPair();
    }

    public static BigInteger computeSharedSecret(DHPrivateKey priv, DHPublicKey pub) {
        return pub.getY().modPow(priv.getX(), pub.getParams().getP());
    }
}
