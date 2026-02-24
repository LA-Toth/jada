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

package me.laszloattilatoth.jada.proxy.ssh.kex;

import java.lang.ref.WeakReference;
import java.util.Objects;

public abstract class WithKeyExchange {
    private final WeakReference<KeyExchange> kex;

    public WithKeyExchange(KeyExchange kex) {
        this.kex = new WeakReference<>(kex);
    }

    public KeyExchange kex() {
        return Objects.requireNonNull(kex.get(), "key exchange cannot be null");
    }
}
