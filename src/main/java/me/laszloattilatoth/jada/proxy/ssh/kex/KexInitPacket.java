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

package me.laszloattilatoth.jada.proxy.ssh.kex;

import me.laszloattilatoth.jada.proxy.ssh.core.Buffer;
import me.laszloattilatoth.jada.proxy.ssh.core.Constant;
import me.laszloattilatoth.jada.proxy.ssh.core.NameListWithIds;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.Packet;

public class KexInitPacket extends KexInitEntries {
    public static final int COOKIE_LEN = 16;

    public boolean follows = false;

    public void readFromPacket(Packet packet) throws Buffer.BufferEndReachedException {
        packet.getByte();   // type
        packet.getBytes(COOKIE_LEN);
        readEntriesFromPacket(packet);
        follows = packet.getBoolean();
        packet.getUint32();  // reserved
    }

    private void readEntriesFromPacket(Packet packet) throws Packet.BufferEndReachedException {
        for (int i = 0; i != ENTRY_MAX; ++i) {
            entries[i] = NameListWithIds.create(packet.getSshString());
        }
    }

    public boolean valid() {
        for (int i = 0; i != ENTRY_NON_EMPTY_MAX; ++i) {
            if (entries[i] == null || entries[i].size() == 0)
                return false;
        }

        return true;
    }

    public void writeToPacket(Packet packet) throws Packet.BufferEndReachedException {
        packet.putByte(Constant.SSH_MSG_KEXINIT);
        for (int i = 0; i != COOKIE_LEN; ++i)
            packet.putByte(0);  // FIXME: Security????
        for (int i = 0; i != ENTRY_MAX; ++i) {
            packet.putString(entries[i].getNameList());
        }
        packet.putBoolean(false);
        packet.putUint32(0);   // reserved
    }
}
