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

package me.laszloattilatoth.jada.proxy.ssh.transportlayer;

import me.laszloattilatoth.jada.proxy.ssh.core.Buffer;
import me.laszloattilatoth.jada.proxy.ssh.helpers.NameListHelper;
import me.laszloattilatoth.jada.util.Logging;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Representing an SSH packet with ByteBuffer.
 */
public class Packet extends Buffer {

    public Packet() {
        super();
    }

    public Packet(byte[] bytes) {
        super(bytes);
    }

    public byte packetType() {
        return getType();
    }

    public void dump() {
        Logger logger = Logging.logger();
        logger.info(() -> String.format("Packet dump follows; packet_type='%d', packet_type_hex='%x', length='%d'",
                getType(), getType(), size));
        Logging.logBytes(logger, this.buffer, this.bufferEnd);
    }

    public ArrayList<String> getNameList() throws BufferEndReachedException {
        int length = getUint32();
        checkPosition(length);

        ArrayList<String> result = NameListHelper.splitNameList(buffer, position, length);
        position += length;

        return result;
    }

    public int[] getNameIdList() throws BufferEndReachedException {
        return NameListHelper.getIdListFromNameArrayList(getNameList());
    }

    public String getLine() {
        if (limitReached())
            return null;
        int pos = position;

        boolean found = false;
        for (; pos < bufferEnd; ++pos) {
            if (buffer[pos] == '\r' || buffer[pos] == '\n') {
                found = true;
                break;
            }
        }
        if (!found)
            return null;

        String result = new String(buffer, position, pos - position);

        pos += (pos != position && buffer[pos] == '\r' && buffer[pos + 1] == '\n') ? 2 : 1;
        position = pos;
        return result;
    }
}
