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

import me.laszloattilatoth.jada.proxy.ssh.transportlayer.TransportLayerException;
import me.laszloattilatoth.jada.util.Sec;

import javax.crypto.KeyAgreement;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;

public class DHKex {
    protected final KeyPairGenerator generator;
    protected final KeyAgreement agreement;
    protected final DH dh;

    public DHKex(String generator, String agreement, DH dh) throws TransportLayerException {
        try {
            this.generator = KeyPairGenerator.getInstance(generator, Sec.provider);
            this.agreement = KeyAgreement.getInstance(agreement, Sec.provider);
        } catch (GeneralSecurityException e) {
            throw new TransportLayerException(e.getMessage());
        }

        this.dh = dh;
    }

    public BigInteger generateE() {
        return new BigInteger("2");
    }
}
