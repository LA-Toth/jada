// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.kex.dh;

import java.math.BigInteger;

public record DHGroup(BigInteger generator, BigInteger modulus) {
}
