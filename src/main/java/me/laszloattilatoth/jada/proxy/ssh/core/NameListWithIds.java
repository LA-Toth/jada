// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.core;

import me.laszloattilatoth.jada.util.Logging;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class NameListWithIds {
    private final String originalNameList;
    private final String nameList;
    private final String[] removedNameList;
    private final int[] nameIdList;

    private NameListWithIds(String nameList) {
        this.originalNameList = nameList;
        this.nameList = Arrays.stream(nameList.split(","))
                .filter(Name::hasName)
                .collect(Collectors.joining(","));
        this.removedNameList = Arrays.stream(nameList.split(","))
                .filter(Name::isUnknownName)
                .toArray(String[]::new);
        this.nameIdList = Arrays.stream(nameList.split(","))
                .filter(Name::hasName)
                .map(Name::getNameId)
                .mapToInt(x -> x)
                .toArray();
    }

    public static NameListWithIds create(String nameList) {
        return new NameListWithIds(nameList);
    }

    public static NameListWithIds createAndLog(String nameList, Logger logger, String name) {
        NameListWithIds n = new NameListWithIds(nameList);
        n.log(logger, name);
        return n;
    }

    public void filter(int[] nameIdList) {

    }

    public int size() {
        return nameIdList.length;
    }

    public String originalNameList() {
        return originalNameList;
    }

    public String nameList() {
        return nameList;
    }

    public String[] removedNameList() {
        return removedNameList;
    }

    public int[] nameIdList() {
        return nameIdList;
    }

    public int firstId() {
        return nameIdList[0];
    }

    public int getFirstMatchingId(NameListWithIds other) {
        Logging.logger().log(Level.FINEST, () -> String.format("Find matching SSH name; this='%s', other='%s'", nameList, other.nameList));
        for (int nameId : nameIdList) {
            for (int otherNameId : other.nameIdList) {
                if (nameId == otherNameId)
                    return nameId;
            }
        }

        return Name.SSH_NAME_UNKNOWN;
    }

    public NameWithId getFirstMatchingNameWithId(NameListWithIds other) {
        return new NameWithId(getFirstMatchingId(other));
    }

    public NameWithId firstNameWithId() {
        return new NameWithId(firstId());
    }

    public void log(Logger logger, String nameListName) {
        logger.info(() -> String.format(
                "Name list '%s': effective_list='%s', complete_list='%s', removed_list='%s'",
                nameListName,
                nameList,
                originalNameList,
                String.join(",", removedNameList)
        ));
    }

    public boolean isFirstNameEquals(NameListWithIds other) {
        return firstId() == other.firstId();
    }
}
