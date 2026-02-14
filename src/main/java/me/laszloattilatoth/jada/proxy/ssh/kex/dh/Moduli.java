/*
 * Copyright 2026 Laszlo Attila Toth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.laszloattilatoth.jada.proxy.ssh.kex.dh;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public record Moduli(int size, BigInteger generator, BigInteger modulus) {
    public static List<Moduli> modulii = new ArrayList<>();

    private static final int MODULI_TYPE_SAFE = 2;
    private static final int MODULI_TEST_COMPOSITE = 0x01;

//   static {
//        // From OpenSSH 10.0p1
//        // cat moduli | tail -n+3  | awk '{ printf "create(\"%s\", %d, %d, %d, %d, \"%s\", \"%s\");\n", $1, $2, $3, $4, $5, $6, $7 }'
//         }

    private static void create(String timestamp, int mtype, int tests, int tries, int size, String generator, String modulus) {
        /* assume possible issues, the caller is generated */
        if (/*mtype < 0 || mtype > 5 ||*/ mtype != MODULI_TYPE_SAFE) {
            return;
        }
        if (tests < 0 || tests > 0x1f || (tests & MODULI_TEST_COMPOSITE) == MODULI_TEST_COMPOSITE || 0 == (tests & ~MODULI_TEST_COMPOSITE)) {
            return;
        }
        if (tries < 0 || tries > (1 << 30)) {
            return;
        }
        if (size < 0 || size > 64 * 1024) {
            return;
        }

        BigInteger p = new BigInteger(modulus, 16);
        if (p.bitCount() != size + 1) {
            return;
        }

        if (p.compareTo(BigInteger.ONE) <= 0) {
            return;
        }

        BigInteger g = new BigInteger(generator, 16);

        modulii.add(new Moduli(size + 1, g, p));
    }
}
