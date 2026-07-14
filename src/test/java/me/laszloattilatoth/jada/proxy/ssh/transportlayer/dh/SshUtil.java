package me.laszloattilatoth.jada.proxy.ssh.transportlayer.dh;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util .HexFormat;

final class SshUtil {

    static void writeString(OutputStream out, byte[] data) throws IOException {
        out.write(ByteBuffer.allocate(4).putInt(data.length).array());
        out.write(data);
    }

    static void writeMpint(OutputStream out, BigInteger bi) throws IOException {
        byte[] raw = bi.toByteArray();
        out.write(ByteBuffer.allocate(4).putInt(raw.length).array());
        out.write(raw);
    }

    static byte[] sha1(byte[] data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        return md.digest(data);
    }

    static byte[] sha256(byte[] data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(data);
    }

    static String hex(byte[] b) {
        return HexFormat.of().formatHex(b);
    }
}
