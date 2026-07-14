package me.laszloattilatoth.jada.proxy.ssh.transportlayer.dh;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DHTest {

    @Test
    public void testDhExchange() throws Exception {

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair hostKey = kpg.generateKeyPair();

        Wrapper w = new Wrapper(
                "SSH-2.0-testclient".getBytes(StandardCharsets.UTF_8),
                "SSH-2.0-testserver".getBytes(StandardCharsets.UTF_8),
                new byte[]{1, 2, 3}, // fake KEXINIT
                new byte[]{4, 5, 6},
                hostKey
        );

        w.calculate();

        byte[] reply = w.getReplyAsArray();
        DerivedKeys keys = w.getDerivedKeys();

        assertNotNull(reply);
        assertNotNull(keys.ivC2S());
        assertNotNull(keys.keyC2S());
    }
}
