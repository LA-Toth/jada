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

import me.laszloattilatoth.jada.proxy.ssh.helpers.NameListHelper;
import me.laszloattilatoth.jada.util.Logging;
import org.apache.sshd.common.util.GenericUtils;
import org.apache.sshd.common.util.buffer.ByteArrayBuffer;

import java.util.logging.Logger;

/**
 * Representing an SSH packet with ByteBuffer.
 */
public class Packet extends ByteArrayBuffer {

    public Packet() {
        super();
    }

    public Packet(byte[] bytes) {
        super(bytes);
    }

    public boolean endReached() {
        return available() == 0;
    }

    public byte packetType() {
        return array()[0];
    }

    public byte[] getCompactArray() {
        int l = this.wpos();
        if (l > 0) {
            byte[] b = new byte[l];
            System.arraycopy(this.array(), 0, b, 0, l);
            return b;
        } else {
            return GenericUtils.EMPTY_BYTE_ARRAY;
        }
    }

    public void dump() {
        Logger logger = Logging.logger();
        logger.info(() -> String.format("Packet dump follows; packet_type='%d', packet_type_hex='%x', length='%d'",
                packetType(), packetType(), wpos()));
        Logging.logBytes(logger, this.array(), this.wpos());
    }

    public int[] getNameIdList() {
        return NameListHelper.getIdListFromNameArrayList(getNameList());
    }

    public void putByte(int i) {
        putByte((byte) i);
    }

    public String getLine() {
        if (available() == 0)
            return null;
        final int rpos = rpos();
        final byte[] data = array();

        boolean found = false;
        int pos = rpos;
        for (; pos < wpos(); ++pos) {
            if (data[pos] == '\r' || data[pos] == '\n') {
                found = true;
                break;
            }
        }
        if (!found)
            return null;

        String result = new String(data, rpos(), pos - rpos());

        pos += (pos != rpos && data[pos] == '\r' && data[pos + 1] == '\n') ? 2 : 1;
        rpos(pos);
        return result;
    }
}
