// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.config;

import java.net.SocketAddress;

public record ProxyConfig(String name, String proxyType, SocketAddress[] addresses, SocketAddress target,
                          ProxyOptions options) {
}
