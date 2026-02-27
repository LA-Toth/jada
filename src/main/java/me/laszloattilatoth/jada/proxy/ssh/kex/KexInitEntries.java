// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.kex;

import me.laszloattilatoth.jada.proxy.ssh.core.NameListWithIds;

public class KexInitEntries {
    public static final int ENTRY_KEX_ALGOS = 0;
    public static final int ENTRY_SERVER_HOST_KEY_ALG = 1;
    public static final int ENTRY_ENC_ALGOS_C2S = 2;
    public static final int ENTRY_ENC_ALGOS_S2C = 3;
    public static final int ENTRY_MAC_ALGOS_C2S = 4;
    public static final int ENTRY_MAC_ALGOS_S2C = 5;
    public static final int ENTRY_COMP_ALGOS_C2S = 6;
    public static final int ENTRY_COMP_ALGOS_S2C = 7;
    public static final int ENTRY_LANG_C2S = 8;
    public static final int ENTRY_LANG_S2C = 9;
    public static final int ENTRY_MAX = ENTRY_LANG_S2C + 1;

    public static final int ENTRY_NON_EMPTY_MAX = ENTRY_COMP_ALGOS_S2C + 1;

    protected final NameListWithIds[] entries = new NameListWithIds[ENTRY_MAX];

    public KexInitEntries() {
        for (int i = 0; i != ENTRY_MAX; ++i) {
            entries[i] = NameListWithIds.create("");
        }
    }

    public void set(int entryIndex, NameListWithIds nameListWithIds) throws KexException {
        if (entryIndex < 0 || entryIndex >= ENTRY_MAX)
            throw new KexException("Entry index is out of range");
        entries[entryIndex] = nameListWithIds;
    }

    public void set(int entryIndex, String nameList) throws KexException {
        if (entryIndex < 0 || entryIndex >= ENTRY_MAX)
            throw new KexException("Entry index is out of range");
        entries[entryIndex] = NameListWithIds.create(nameList);
    }

    public NameListWithIds get(int entryIndex) throws KexException {
        if (entryIndex < 0 || entryIndex >= ENTRY_MAX)
            throw new KexException("Entry index is out of range");
        return entries[entryIndex];
    }
}
