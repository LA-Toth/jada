/*
 * Copyright 2020-2026 Laszlo Attila Toth
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

import me.laszloattilatoth.jada.proxy.ssh.core.Constant;
import me.laszloattilatoth.jada.proxy.ssh.core.NameListWithIds;
import me.laszloattilatoth.jada.proxy.ssh.core.SecureRandomWithByteArray;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.Packet;

public class KexInitPacket extends KexInitEntries {
    public static final int COOKIE_LEN = 16;
    private static final SecureRandomWithByteArray secureRandom = new SecureRandomWithByteArray(COOKIE_LEN);
    public boolean follows = false;

    public void readFromPacket(Packet packet) {
        packet.getByte();   // type
        byte[] cookie = new byte[COOKIE_LEN];
        packet.getRawBytes(cookie);
        readEntriesFromPacket(packet);
        follows = packet.getBoolean();
        packet.getUInt();  // reserved
    }

    private void readEntriesFromPacket(Packet packet) {
        for (int i = 0; i != ENTRY_MAX; ++i) {
            entries[i] = NameListWithIds.create(packet.getString());
        }
    }

    public boolean valid() {
        for (int i = 0; i != ENTRY_NON_EMPTY_MAX; ++i) {
            if (entries[i] == null || entries[i].size() == 0)
                return false;
        }

        return true;
    }

    public boolean isFirstNameEquals(int index, KexInitPacket other) {
        return entries[index].isFirstNameEquals(other.entries[index]);
    }

    public void writeToPacket(Packet packet) {
        packet.wpos(0);
        packet.putByte(Constant.SSH_MSG_KEXINIT);
        packet.putRawBytes(secureRandom.getSecureBytes());
        for (int i = 0; i != ENTRY_MAX; ++i) {
            packet.putString(entries[i].nameList());
        }
        packet.putBoolean(false);
        packet.putInt(0);   // reserved
    }
}
