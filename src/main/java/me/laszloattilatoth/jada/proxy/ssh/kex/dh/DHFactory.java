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

import me.laszloattilatoth.jada.proxy.ssh.core.Name;
import me.laszloattilatoth.jada.proxy.ssh.core.Side;
import me.laszloattilatoth.jada.proxy.ssh.kex.digest.Digest;
import me.laszloattilatoth.jada.proxy.ssh.kex.digest.DigestFactory;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.TransportLayerException;

public class DHFactory {

    public static DH createFromKexAlgoId(int sshNameId, String ownSshIDString, String peerSshIdString, byte[] ownKexInit, byte[] peerKexInit, Side side) throws TransportLayerException {
        Digest digest = switch (sshNameId) {
            case Name.SSH_NAME_DIFFIE_HELLMAN_GROUP1_SHA1, Name.SSH_NAME_DIFFIE_HELLMAN_GROUP14_SHA1 -> DigestFactory.createSHA1();
            case Name.SSH_NAME_DIFFIE_HELLMAN_GROUP14_SHA256 -> DigestFactory.createSHA256();
            default -> DigestFactory.createSHA512();
        };
        DH dh = switch (sshNameId) {
            case Name.SSH_NAME_DIFFIE_HELLMAN_GROUP1_SHA1 -> DHGroup.createGroup1(digest);
            case Name.SSH_NAME_DIFFIE_HELLMAN_GROUP14_SHA1, Name.SSH_NAME_DIFFIE_HELLMAN_GROUP14_SHA256 -> DHGroup.createGroup14(digest);
            case Name.SSH_NAME_DIFFIE_HELLMAN_GROUP16_SHA512 -> DHGroup.createGroup16(digest);
            case Name.SSH_NAME_DIFFIE_HELLMAN_GROUP18_SHA512 -> DHGroup.createGroup18(digest);
            default -> throw new TransportLayerException("Unexpected kexalgo");
        };

        if (side.isClient())
            dh.init(peerSshIdString, ownSshIDString, peerKexInit, ownKexInit);
        else
            dh.init(ownSshIDString, peerSshIdString, ownKexInit, peerKexInit);

        return dh;
    }
}
