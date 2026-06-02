// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.config;

import java.net.SocketAddress;

/**
 * Immutable runtime descriptor for one configured proxy instance.
 *
 * @param name user-defined proxy instance name
 * @param proxyType registered proxy type identifier
 * @param addresses listener addresses where the proxy accepts connections
 * @param target upstream target address, or {@code null} for inband mode
 * @param options proxy-specific options object
 */
public record ProxyConfig(String name, String proxyType, SocketAddress[] addresses, SocketAddress target,
                          ProxyOptions options) {
}
