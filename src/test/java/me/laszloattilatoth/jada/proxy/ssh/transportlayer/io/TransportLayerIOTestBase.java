package me.laszloattilatoth.jada.proxy.ssh.transportlayer.io;

import me.laszloattilatoth.jada.core.TestRandom;
import me.laszloattilatoth.jada.proxy.ssh.core.Direction;
import me.laszloattilatoth.jada.proxy.ssh.crypto.CipherSuite;
import me.laszloattilatoth.jada.proxy.ssh.crypto.CryptoContext;
import me.laszloattilatoth.jada.proxy.ssh.crypto.CryptoContextFactory;
import me.laszloattilatoth.jada.proxy.ssh.crypto.SessionKeys;
import me.laszloattilatoth.jada.proxy.ssh.kex.KexOutput;
import me.laszloattilatoth.jada.proxy.ssh.kex.algorithm.CipherRegistry;
import me.laszloattilatoth.jada.proxy.ssh.kex.algorithm.CipherSpec;
import me.laszloattilatoth.jada.util.logging.LoggerFactory;

import java.security.SecureRandom;

public class TransportLayerIOTestBase {
    protected TransportLayerInput createTransportLayerInput() {
        return new InboundTransportLayerIO(LoggerFactory.getNulLogger("test"));
    }

    protected TransportLayerOutput createTransportLayerOutput() {
        return new OutboundTransportLayerIO(LoggerFactory.getNulLogger("test"));
    }

    protected CryptoContext createCryptoContext(Direction direction) {
        CipherSpec cipherSpec = CipherRegistry.byName("aes128-ctr");
        CipherSuite suite = new CipherSuite();
        suite.setCipherSpec(cipherSpec);

        KexOutput output = createKexOutput(cipherSpec);

        SessionKeys sessionKeys = SessionKeys.createClientSessionKeys(output); // server's keys are the same here

        return new CryptoContextFactory().createContext(suite, sessionKeys, direction);
    }

    private KexOutput createKexOutput(CipherSpec cipherSpec) {
        SecureRandom secureRandom = new TestRandom(0x22deada458beefL);
        byte[] iv = new byte[cipherSpec.ivLen()];
        byte[] enc_key = new byte[cipherSpec.keyLen()];

        // TODO: actually implement this
        byte[] mac_key = new byte[42];

        secureRandom.nextBytes(iv);
        secureRandom.nextBytes(enc_key);
        secureRandom.nextBytes(mac_key);

        return new KexOutput(iv, iv, enc_key, enc_key, mac_key, mac_key);
    }
}
