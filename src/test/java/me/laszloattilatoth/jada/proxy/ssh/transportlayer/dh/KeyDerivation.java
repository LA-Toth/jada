package me.laszloattilatoth.jada.proxy.ssh.transportlayer.dh;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;

public final class KeyDerivation {

    public static DerivedKeys deriveKeys(BigInteger K, byte[] H, byte[] sessionId) throws Exception {
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
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        writeMpint(out, K);
        out.write(H);
        out.write((byte) letter);
        out.write(sessionId);

        return sha1.digest(out.toByteArray());
    }

    private static void writeMpint(OutputStream out, BigInteger bi) throws IOException {
        byte[] raw = bi.toByteArray();
        out.write(ByteBuffer.allocate(4).putInt(raw.length).array());
        out.write(raw);
    }
}
