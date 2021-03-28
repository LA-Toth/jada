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
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.TransportLayerException;

public class DHFactory {

    public static DiffieHellman createFromKexAlgoId(int sshNameId) throws TransportLayerException {
        return switch (sshNameId) {
            case Name.SSH_NAME_DIFFIE_HELLMAN_GROUP1_SHA1 -> DiffieHellmanGroup.createGroup1();
            case Name.SSH_NAME_DIFFIE_HELLMAN_GROUP14_SHA1, Name.SSH_NAME_DIFFIE_HELLMAN_GROUP14_SHA256 -> DiffieHellmanGroup.createGroup14();
            case Name.SSH_NAME_DIFFIE_HELLMAN_GROUP16_SHA512 -> DiffieHellmanGroup.createGroup16();
            case Name.SSH_NAME_DIFFIE_HELLMAN_GROUP18_SHA512 -> DiffieHellmanGroup.createGroup18();
            default -> throw new TransportLayerException("Unexpected kexalgo");
        };
    }


}
