package me.laszloattilatoth.jada.proxy.ssh.transportlayer.dh;

import java.math.BigInteger;

public final class DHGroup14 {
    public static final BigInteger P = new BigInteger(
            "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1" +
                    "29024E088A67CC74020BBEA63B139B22514A08798E3404DD" +
                    "EF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245" +
                    "E485B576625E7EC6F44C42E9A63A36210000000000090563", 16);

    public static final BigInteger G = BigInteger.valueOf(2);
}
