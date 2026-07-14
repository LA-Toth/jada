package me.laszloattilatoth.jada.proxy.ssh.transportlayer.dh;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

//SSH_MSG_KEXDH_REPLY
public final class ServerReply {

    public static byte[] build(PublicKey hostKey,
                               BigInteger f,
                               byte[] H,
                               PrivateKey hostPriv) throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        out.write(31); // SSH_MSG_KEXDH_REPLY
        writeString(out, hostKey.getEncoded());
        writeMpint(out, f);

        Signature sig = Signature.getInstance("SHA1withRSA");
        sig.initSign(hostPriv);
        sig.update(H);
        byte[] signature = sig.sign();

        writeString(out, signature);

        return out.toByteArray();
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
