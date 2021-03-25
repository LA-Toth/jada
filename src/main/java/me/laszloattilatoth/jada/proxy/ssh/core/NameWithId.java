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
