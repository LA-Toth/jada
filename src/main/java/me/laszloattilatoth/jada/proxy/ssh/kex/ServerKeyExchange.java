// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.kex;

import me.laszloattilatoth.jada.proxy.ssh.core.Constant;
import me.laszloattilatoth.jada.proxy.ssh.kex.dh.DHGServer;
import me.laszloattilatoth.jada.proxy.ssh.kex.dh.DHKexFactory;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.Packet;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.TransportLayer;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.TransportLayerException;
import me.laszloattilatoth.jada.util.security.SecurityUtils;
import org.apache.sshd.common.kex.AbstractDH;

public class ServerKeyExchange extends KeyExchange {

    protected AbstractDH dh;

    public ServerKeyExchange(TransportLayer transportLayer) {
        super(transportLayer);
    }

    private DHGServer dhKex() {
        return (DHGServer) dhKex;
    }

    /// RFC 4253 7.1.  Algorithm Negotiation (SSH_MSG_KEXINIT)
    public void processMsgKexInit(Packet packet) throws TransportLayerException {
        super.processMsgKexInit(packet);
        transportLayer().registerHandler(Constant.SSH_MSG_KEXDH_INIT, this::processKexDhInit, "SSH_MSG_KEXDH_INIT");
    }

    public void processKexDhInit(Packet packet) throws TransportLayerException {
        try {
            hostKey = SecurityUtils.loadHostKey(hostKeyAlgName.name());
            dhKex = DHKexFactory.createServer(this,
                    kexAlgorithmSpec,
                    Constant.SSH_ID_STRING,
                    transportLayer().peerIDString(),
                    kexState.getOwnKexInit(),
                    kexState.getPeerKexInit()
            );
            packet.getByte();
            dhKex().processKexDhInit(packet);
            setKexOutput(new KexOutputFactory().create(dhKex().getHash(), dhKex().getK(),
                    kexState.getOwnKexInit(),
                    kexState.getOwnKexInit(),
                    clientNewKeys(), serverNewKeys()));
        } catch (TransportLayerException e) {
            throw e;
        } catch (Exception e) {
            throw new TransportLayerException(e.getMessage());
        }
        if (!packet.endReached())
            throw new TransportLayerException("Unexpected additional data found in Packet");
    }
}
