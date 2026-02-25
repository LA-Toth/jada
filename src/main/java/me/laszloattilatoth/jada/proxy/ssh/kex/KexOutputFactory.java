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

package me.laszloattilatoth.jada.proxy.ssh.kex;

import org.apache.sshd.common.digest.Digest;
import org.apache.sshd.common.util.buffer.ByteArrayBuffer;

import java.util.Arrays;

public class KexOutputFactory {
    public final KexOutput create(Digest hash, byte[] K, byte[] H, byte[] sessionId, NewKeys clientNewKeys, NewKeys serverNewKeys) throws Exception {
        byte[] iv_c2s = deriveKey(hash, K, H, 'A', sessionId, clientNewKeys.cipherSpec.ivLen());
        byte[] iv_s2c = deriveKey(hash, K, H, 'B', sessionId, serverNewKeys.cipherSpec.ivLen());
        byte[] enc_key_c2s = deriveKey(hash, K, H, 'C', sessionId, clientNewKeys.cipherKeyLen());
        byte[] enc_key_s2c = deriveKey(hash, K, H, 'D', sessionId, serverNewKeys.cipherKeyLen());
        byte[] integrity_key_c2s = deriveKey(hash, K, H, 'E', sessionId, clientNewKeys.macSpec.keyLen());
        byte[] integrity_key_s2c = deriveKey(hash, K, H, 'F', sessionId, serverNewKeys.macSpec.keyLen());

        return new KexOutput(iv_c2s, iv_s2c, enc_key_c2s, enc_key_s2c, integrity_key_c2s, integrity_key_s2c);
    }

    /**
     * Derive a key or IV as per as RFC 4253, 7.2.  Output from Key Exchange
     *
     * @param key_id Identifies which key (IV) is generated, single char 'A' to 'F'
     */
    private byte[] deriveKey(Digest hash, byte[] K, byte[] H, char key_id, byte[] sessionId, long neededBytes) throws Exception {
        hash.init();

        ByteArrayBuffer buffer = new ByteArrayBuffer();
        //buffer.putMPInt(K);
        buffer.putRawBytes(K);
        buffer.putRawBytes(H);
        buffer.putByte((byte) key_id);
        buffer.putRawBytes(sessionId);
        hash.update(buffer.getCompactData());
        byte[] result = hash.digest();

        ByteArrayBuffer key = new ByteArrayBuffer();
        key.putRawBytes(result);
        while (key.wpos() < neededBytes) {
            // K || H || previous
            ByteArrayBuffer t = new ByteArrayBuffer();
            t.putMPInt(K);
            t.putRawBytes(H);
            t.putBytes(key.getCompactData());

            hash.init();
            hash.update(buffer.getCompactData());
            byte[] more = hash.digest();
            key.putBytes(more);
        }
        byte[] outKey = key.getCompactData();
        return Arrays.copyOf(outKey, (int) neededBytes);
    }
}
