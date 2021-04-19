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

import me.laszloattilatoth.jada.proxy.ssh.kex.digest.Digest;
import org.bouncycastle.crypto.params.DHParameters;

import java.math.BigInteger;

public class DHGroup extends DH {
    public DHParameters params;

    private DHGroup(BigInteger gen, BigInteger modulus, Digest digest) {
        super(digest);
        params = new DHParameters(modulus, null, gen);
    }

    static DHGroup createGroup1(Digest digest) {
        return new DHGroup(Constants.gen, Constants.group1Mod, digest);
    }

    static DHGroup createGroup14(Digest digest) {
        return new DHGroup(Constants.gen, Constants.group14Mod, digest);
    }

    static DHGroup createGroup16(Digest digest) {
        return new DHGroup(Constants.gen, Constants.group16Mod, digest);
    }

    static DHGroup createGroup18(Digest digest) {
        return new DHGroup(Constants.gen, Constants.group18Mod, digest);
    }
}
