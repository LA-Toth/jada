// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.kex.dh;

import java.math.BigInteger;

public record DHGroup(BigInteger generator, BigInteger modulus) {
}
