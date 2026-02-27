// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

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
