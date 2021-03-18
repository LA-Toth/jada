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

package me.laszloattilatoth.jada.proxy.ssh.core;

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

    public String getOriginalNameList() {
        return originalNameList;
    }

    public String getNameList() {
        return nameList;
    }

    public String[] getRemovedNameList() {
        return removedNameList;
    }

    public int[] getNameIdList() {
        return nameIdList;
    }

    public int getFirstId() {
        return nameIdList[0];
    }

    public int getFirstMatchingId(NameListWithIds other) {
        // TODO: provide logger from the current thread
        //Util.sshLogger().log(Level.FINEST, () -> String.format("Find matching SSH name; this='%s', other='%s'", nameList, other.nameList));
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

    public NameWithId getFirstNameWithId() {
        return new NameWithId(getFirstId());
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

    public boolean firstEqual(NameListWithIds other) {
        return getFirstId() == other.getFirstId();
    }
}
