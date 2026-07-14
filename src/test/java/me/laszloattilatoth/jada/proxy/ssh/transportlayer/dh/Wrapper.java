package me.laszloattilatoth.jada.proxy.ssh.transportlayer.dh;

import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;
import java.math.BigInteger;
import java.security.KeyPair;

public final class Wrapper {

    private final byte[] clientIdent;
    private final byte[] serverIdent;
    private final byte[] clientKexInit;
    private final byte[] serverKexInit;
    private final KeyPair serverHostKey;

    private byte[] serverDhReply;
    private DerivedKeys derived;

    public Wrapper(byte[] clientIdent,
                   byte[] serverIdent,
                   byte[] clientKexInit,
                   byte[] serverKexInit,
                   KeyPair serverHostKey) {
        this.clientIdent = clientIdent;
        this.serverIdent = serverIdent;
        this.clientKexInit = clientKexInit;
        this.serverKexInit = serverKexInit;
        this.serverHostKey = serverHostKey;
    }

    public void calculate() throws Exception {
        // RFC 4253 DH group14
        BigInteger p = DHGroup14.P;
        BigInteger g = DHGroup14.G;

        // Client ephemeral
        KeyPair clientDh = DHUtil.generateKeyPair(p, g);
        BigInteger e = ((DHPublicKey) clientDh.getPublic()).getY();

        // Server ephemeral
        KeyPair serverDh = DHUtil.generateKeyPair(p, g);
        BigInteger f = ((DHPublicKey) serverDh.getPublic()).getY();

        // Shared secret K
        BigInteger K = DHUtil.computeSharedSecret(
                (DHPrivateKey) serverDh.getPrivate(),
                (DHPublicKey) clientDh.getPublic()
        );

        // Build exchange hash H
        byte[] H = ExchangeHash.compute(
                clientIdent,
                serverIdent,
                clientKexInit,
                serverKexInit,
                serverHostKey.getPublic(),
                e,
                f,
                K
        );

        // Session ID = H of first key exchange
        byte[] sessionId = H;

        // Derive keys
        derived = KeyDerivation.deriveKeys(K, H, sessionId);

        // Build server reply packet (without padding)
        serverDhReply = ServerReply.build(
                serverHostKey.getPublic(),
                f,
                H,
                serverHostKey.getPrivate()
        );
    }

    public byte[] getReplyAsArray() {
        return serverDhReply;
    }

    public DerivedKeys getDerivedKeys() {
        return derived;
    }
}
