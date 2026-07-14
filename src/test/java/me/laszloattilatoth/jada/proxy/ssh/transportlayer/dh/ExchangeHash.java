package me.laszloattilatoth.jada.proxy.ssh.transportlayer.dh;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.PublicKey;

public final class ExchangeHash {

    public static byte[] compute(
            byte[] V_C,
            byte[] V_S,
            byte[] I_C,
            byte[] I_S,
            PublicKey hostKey,
            BigInteger e,
            BigInteger f,
            BigInteger K
    ) throws Exception {

        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        writeString(out, V_C);
        writeString(out, V_S);
        writeString(out, I_C);
        writeString(out, I_S);
        writeString(out, hostKey.getEncoded());
        writeMpint(out, e);
        writeMpint(out, f);
        writeMpint(out, K);

        return sha1.digest(out.toByteArray());
    }

    private static void writeString(OutputStream out, byte[] data) throws IOException {
        out.write(ByteBuffer.allocate(4).putInt(data.length).array());
        out.write(data);
    }

    private static void writeMpint(OutputStream out, BigInteger bi) throws IOException {
        byte[] raw = bi.toByteArray();
        out.write(ByteBuffer.allocate(4).putInt(raw.length).array());
        out.write(raw);
    }
}
