// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.kex;

import me.laszloattilatoth.jada.proxy.ssh.transportlayer.StoringTransportLayer;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.TransportLayer;

import java.io.IOException;

public class StoringServerKeyExchange extends ServerKeyExchange {
    public StoringServerKeyExchange(TransportLayer transportLayer) {
        super(transportLayer);
    }

    @Override
    protected void loadHostKey() {
        super.loadHostKey();
        try {
            writeOutBytes(hostKey.getPrivate().getEncoded());
            writeOutBytes(hostKey.getPublic().getEncoded());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeOutBytes(byte[] bytes) throws IOException {
        ((StoringTransportLayer) transportLayer()).writeBytesToFile(bytes, true);
    }
}
