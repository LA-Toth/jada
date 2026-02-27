// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.config;

import java.net.SocketAddress;

public record ProxyConfig(String name, String proxyType, SocketAddress[] addresses, SocketAddress target,
                          ProxyOptions options) {
}
