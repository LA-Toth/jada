/*
 * Copyright 2021 Laszlo Attila Toth
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

import org.bouncycastle.crypto.params.DHParameters;

import java.math.BigInteger;

public class DiffieHellmanGroup  extends DiffieHellman {
    public DHParameters params;

    public DiffieHellmanGroup(BigInteger gen, BigInteger modulus) {
        params = new DHParameters(modulus, null, gen);
    }

    public static DiffieHellmanGroup createGroup1() {
        return new DiffieHellmanGroup(Constants.group1Gen, Constants.group1Mod);
    }

    public static DiffieHellmanGroup createGroup14() {
        return new DiffieHellmanGroup(Constants.group14Gen, Constants.group14Mod);
    }

    public static DiffieHellmanGroup createGroup16() {
        return new DiffieHellmanGroup(Constants.group16Gen, Constants.group16Mod);
    }

    public static DiffieHellmanGroup createGroup18() {
        return new DiffieHellmanGroup(Constants.group18Gen, Constants.group18Mod);
    }
}
