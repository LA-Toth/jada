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

package me.laszloattilatoth.jada.proxy.ssh.helpers;

import me.laszloattilatoth.jada.proxy.ssh.core.Name;

import java.util.ArrayList;
import java.util.List;

public class NameListHelper {

    public static ArrayList<String> splitNameList(byte[] buffer, int startingPosition, int length) {
        int startPos = startingPosition;
        int endPos = startingPosition;
        ArrayList<String> result = new ArrayList<>();

        for (int i = 0; i < length; ++i) {
            if (buffer[endPos] == ',') {
                result.add(new String(buffer, startPos, endPos - startPos - 1));
                startPos = endPos + 1;
            }
            endPos++;
        }

        result.add(new String(buffer, startPos, endPos - startPos - 1));
        return result;
    }

    public static int[] getIdListFromNameList(byte[] buffer, int startingPosition, int length) {
        ArrayList<String> names = splitNameList(buffer, startingPosition, length);
        return getIdListFromNameArrayList(names);
    }

    public static ArrayList<String> splitNameList(byte[] buffer) {
        return splitNameList(buffer, 0, buffer.length);
    }

    public static int[] getIdListFromNameList(byte[] buffer) {
        ArrayList<String> names = splitNameList(buffer);
        return getIdListFromNameArrayList(names);
    }

    public static ArrayList<String> splitNameList(String s) {
        return splitNameList(s.getBytes());
    }

    public static int[] getIdListFromNameList(String s) {
        ArrayList<String> names = splitNameList(s);
        return getIdListFromNameArrayList(names);
    }

    public static int[] getIdListFromNameArrayList(List<String> names) {
        int[] nameIds = new int[names.size() + 1];
        int actualCount = 0;
        int nextPos = 0;
        for (String name : names) {
            nameIds[nextPos] = Name.getNameId(name);
            if (nameIds[nextPos] != 0)
                actualCount++;
        }

        if (actualCount != names.size()) {
            // not all known
            int[] newIds = new int[actualCount + 1];
            nextPos = 0;
            for (int nameId : nameIds) {
                if (nameId == 0)
                    continue;

                newIds[nextPos++] = nameId;
            }
            nameIds = newIds;
        }

        nameIds[nameIds.length - 1] = 0;
        return nameIds;
    }
}
