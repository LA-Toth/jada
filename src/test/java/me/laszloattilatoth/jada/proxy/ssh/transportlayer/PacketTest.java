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

package me.laszloattilatoth.jada.proxy.ssh.transportlayer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PacketTest {

    @Test
    public void testGetLine() {
        byte[] buffer = "first line\r\nsecond line\nthird line\n".getBytes();
        Packet packet = new Packet(buffer);

        assertEquals("first line", packet.getLine());
        assertEquals("second line", packet.getLine());
        assertEquals("third line", packet.getLine());
        assertTrue(packet.endReached());

        packet.putRawBytes("last line\n".getBytes());
        assertEquals("last line", packet.getLine());
        assertTrue(packet.endReached());
    }
}
