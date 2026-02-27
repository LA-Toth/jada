// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.kex;

import me.laszloattilatoth.jada.proxy.ssh.transportlayer.TransportLayerException;

public class KexException extends TransportLayerException {
    public KexException(String s) {
        super(s);
    }
}
