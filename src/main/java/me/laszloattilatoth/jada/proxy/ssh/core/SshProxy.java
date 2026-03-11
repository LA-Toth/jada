// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.core;

import me.laszloattilatoth.jada.proxy.core.Proxy;
import me.laszloattilatoth.jada.proxy.ssh.Options;

public interface SshProxy extends Proxy {
    Options options();

    boolean shouldQuit();
}
