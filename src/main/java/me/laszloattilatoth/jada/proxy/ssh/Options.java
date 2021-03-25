/*
 * Copyright 2020-2021 Laszlo Attila Toth
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

package me.laszloattilatoth.jada.proxy.ssh;

import me.laszloattilatoth.jada.config.ProxyOptions;
import me.laszloattilatoth.jada.proxy.ssh.core.Side;

public class Options extends ProxyOptions {
    public final SideOptions clientSide = new SideOptions();
    public final SideOptions serverSide = new SideOptions();

    public final SideOptions sideOptions(Side side) {
        return side == Side.CLIENT ? clientSide : serverSide;
    }

    public static class SideOptions {
        public String kexAlgorithms = "diffie-hellman-group14-sha1";
        public String hostkeyAlgorithms = "ssh-rsa";
        public String encryptionAlgorithms = "aes128-ctr";
        public String macAlgorithms = "hmac-sha1";
        public String compressionAlgorithms = "none";
    }
}
