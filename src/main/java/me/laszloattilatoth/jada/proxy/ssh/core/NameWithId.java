// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.core;

public class NameWithId {

    private final String name;
    private final int nameId;

    public NameWithId(String name, int nameId) {
        this.name = name;
        this.nameId = nameId;
    }

    public NameWithId(int nameId) {
        this.name = Name.getName(nameId);
        this.nameId = nameId;
    }

    public String name() {
        return name;
    }

    public int nameId() {
        return nameId;
    }

    public boolean isValid(boolean enableNone) {
        return this.nameId != Name.SSH_NAME_UNKNOWN && (enableNone || this.nameId != Name.SSH_NAME_NONE);
    }

    public boolean isValid() {
        return this.isValid(false);
    }
}
