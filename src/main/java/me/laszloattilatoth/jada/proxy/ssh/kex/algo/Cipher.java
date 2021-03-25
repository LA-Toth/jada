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

package me.laszloattilatoth.jada.proxy.ssh.kex.algo;

public record Cipher(String name, int nameId, long blockSize, long keyLen, long ivLen, long authLen, long flags) {
    // flags are used to map the name to the actual algorithms
    public static int FLAG_CBC = 1;
    public static int FLAG_CHACHAPOLY = 1 << 1; // unused, exists in OpenSSH
    public static int FLAG_AES_CTR = 1 << 2;
    public static int FLAG_3DES = 1 << 3;
    public static int FLAG_AES = 1 << 4;
}
