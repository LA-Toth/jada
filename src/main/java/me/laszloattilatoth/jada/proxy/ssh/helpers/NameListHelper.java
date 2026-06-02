// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.helpers;

import me.laszloattilatoth.jada.proxy.ssh.core.Name;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for splitting SSH name-lists and converting names to internal name IDs.
 *
 * <p>The SSH protocol represents algorithm lists as comma-separated name-lists. This helper
 * provides convenience methods for parsing those lists from byte arrays or strings and mapping
 * the resulting names to IDs via {@link Name#getNameId(String)}.
 */
public class NameListHelper {

    /**
     * Utility class; instances are not allowed.
     */
    private NameListHelper() {
        throw new UnsupportedOperationException("No instance allowed");
    }

    /**
     * Splits a comma-separated name-list contained in a byte buffer.
     *
     * @param buffer the source buffer containing the name-list bytes
     * @param startingPosition the first position of the name-list in {@code buffer}
     * @param length the number of bytes to process from {@code startingPosition}
     * @return the parsed list of names in encounter order
     */
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

    /**
     * Parses a name-list from a byte buffer and converts names to internal IDs.
     *
     * @param buffer the source buffer containing the name-list bytes
     * @param startingPosition the first position of the name-list in {@code buffer}
     * @param length the number of bytes to process from {@code startingPosition}
     * @return a zero-terminated array of known name IDs; unknown names are omitted
     */
    public static int[] getIdListFromNameList(byte[] buffer, int startingPosition, int length) {
        ArrayList<String> names = splitNameList(buffer, startingPosition, length);
        return getIdListFromNameArrayList(names);
    }

    /**
     * Splits a complete byte array as a comma-separated name-list.
     *
     * @param buffer the source buffer containing only name-list bytes
     * @return the parsed list of names in encounter order
     */
    public static ArrayList<String> splitNameList(byte[] buffer) {
        return splitNameList(buffer, 0, buffer.length);
    }

    /**
     * Parses a complete byte array as a name-list and converts names to internal IDs.
     *
     * @param buffer the source buffer containing only name-list bytes
     * @return a zero-terminated array of known name IDs; unknown names are omitted
     */
    public static int[] getIdListFromNameList(byte[] buffer) {
        ArrayList<String> names = splitNameList(buffer);
        return getIdListFromNameArrayList(names);
    }

    /**
     * Splits a comma-separated name-list represented as a string.
     *
     * @param s the source name-list string
     * @return the parsed list of names in encounter order
     */
    public static ArrayList<String> splitNameList(String s) {
        return splitNameList(s.getBytes());
    }

    /**
     * Parses a name-list string and converts names to internal IDs.
     *
     * @param s the source name-list string
     * @return a zero-terminated array of known name IDs; unknown names are omitted
     */
    public static int[] getIdListFromNameList(String s) {
        ArrayList<String> names = splitNameList(s);
        return getIdListFromNameArrayList(names);
    }

    /**
     * Converts a list of name strings to a zero-terminated array of known IDs.
     *
     * <p>Names that do not map to a known ID are removed from the returned array.
     *
     * @param names the list of names to convert
     * @return a zero-terminated array containing only known name IDs
     */
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
