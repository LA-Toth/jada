// SPDX-License-Identifier: Apache-2.0
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.kex;

import me.laszloattilatoth.jada.proxy.ssh.Options;
import me.laszloattilatoth.jada.proxy.ssh.algorithm.HostKeyAlgorithmRegistry;
import me.laszloattilatoth.jada.proxy.ssh.algorithm.HostKeyAlgorithmSpec;
import me.laszloattilatoth.jada.proxy.ssh.core.Constant;
import me.laszloattilatoth.jada.proxy.ssh.core.Name;
import me.laszloattilatoth.jada.proxy.ssh.core.NameListWithIds;
import me.laszloattilatoth.jada.proxy.ssh.core.NameWithId;
import me.laszloattilatoth.jada.proxy.ssh.helpers.LoggerHelper;
import me.laszloattilatoth.jada.proxy.ssh.kex.algorithm.KexAlgorithmRegistry;
import me.laszloattilatoth.jada.proxy.ssh.kex.algorithm.KexAlgorithmSpec;
import me.laszloattilatoth.jada.proxy.ssh.kex.dh.mina.AbstractDHKeyExchange;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.Packet;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.TransportLayer;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.TransportLayerException;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.WithTransportLayer;

import java.io.IOException;
import java.security.KeyPair;

public abstract class KeyExchange extends WithTransportLayer {
    protected final NewKeys[] newKeys = new NewKeys[Constant.MODE_MAX];
    protected final KexState kexState;
    //protected State state = State.INITIAL;
    protected NameWithId kexName;
    protected NameWithId hostKeyAlgName;
    protected KexAlgorithmSpec kexAlgorithmSpec = null;
    protected HostKeyAlgorithmSpec hostHostKeyAlgorithmSpec = null;
    protected KeyPair hostKey;
    protected AbstractDHKeyExchange dhKex;

    private KexOutput kexOutput;

    public KeyExchange(TransportLayer transportLayer) {
        super(transportLayer);
        Options.SideOptions options = transportLayer.proxy().options().sideOptions(side);
        kexState = new KexState(options);
    }

    public NewKeys clientNewKeys() {
        return side.isClient() ? this.newKeys[Constant.MODE_IN] : this.newKeys[Constant.MODE_OUT];
    }

    public NewKeys serverNewKeys() {
        return side.isServer() ? this.newKeys[Constant.MODE_IN] : this.newKeys[Constant.MODE_OUT];
    }

    public int outputBlockSize() {
        if (this.newKeys[Constant.MODE_OUT] == null) {
            return 8;
        } else {
            return (int) this.newKeys[Constant.MODE_OUT].cipherSpec.blockSize();
        }
    }

    /*
    public State getState() {
        return state;
    }
    */

    public KeyPair getHostKey() {
        return hostKey;
    }

    public NameWithId hostKeyAlgName() {
        return hostKeyAlgName;
    }

    public void sendInitialMsgKexInit() throws KexException {
        if (kexState.getOwnKexInit() == null) {
            Packet pkt = new Packet();
            kexState.getOwnInitPacket().writeToPacket(pkt);
            kexState.setOwnKexInit(pkt.getCompactData());
        }

        try {
            transportLayer().writePacket(kexState.getOwnKexInit(), kexState.getOwnKexInit().length);
        } catch (IOException e) {
            logger.severe(() -> String.format("Unable to send SSH_MSG_KEXINIT; error='%s'", e.getMessage()));
            throw new KexException(e.getMessage());
        }
    }

    /// RFC 4253 7.1.  Algorithm Negotiation (SSH_MSG_KEXINIT)
    public void processMsgKexInit(Packet packet) throws TransportLayerException {
        transportLayer().unregisterHandler(Constant.SSH_MSG_KEXINIT);

        KexInitPacket initPacket = new KexInitPacket();
        initPacket.readFromPacket(packet);

        if (!initPacket.valid()) {
            logger.severe("Peer KEXINIT packet contains at least algorithm list which is empty or contains only unknown algos;");
            throw new KexException("Unable to process SSH_MSG_KEXINIT, no known algorithms;");
        }

        // TODO: rekeying - send our own kex packet
        kexState.setPeerInitPacket(initPacket);
        kexState.setPeerKexInit(packet.getCompactArray());
        chooseAlgos();
        prepareDH();
    }

    // Validated/partially based on OpenSSH kex.c: kex_choose_conf
    private void chooseAlgos() throws TransportLayerException {
        KexInitEntries client = side.isClient() ? kexState.getPeerInitPacket() : kexState.getOwnInitPacket();
        KexInitEntries server = side.isClient() ? kexState.getOwnInitPacket() : kexState.getPeerInitPacket();

        // not checking ext_info_c - RFC 8308

        // Choose all algos one by one, and throw exception if no matching algo
        chooseKex(client, server);
        chooseHostKeyAlg(client, server);

        for (int mode = Constant.MODE_IN; mode != Constant.MODE_MAX; ++mode) {
            boolean c2s = (side.isClient() && mode == Constant.MODE_IN) || (side.isServer() && mode == Constant.MODE_OUT);
            int encIdx = c2s ? KexInitEntries.ENTRY_ENC_ALGOS_C2S : KexInitEntries.ENTRY_ENC_ALGOS_S2C;
            int macIdx = c2s ? KexInitEntries.ENTRY_MAC_ALGOS_C2S : KexInitEntries.ENTRY_MAC_ALGOS_S2C;
            int compIdx = c2s ? KexInitEntries.ENTRY_COMP_ALGOS_C2S : KexInitEntries.ENTRY_COMP_ALGOS_S2C;

            NewKeys newkeys = new NewKeys();
            this.newKeys[mode] = newkeys;
            chooseEncAlg(newkeys, client, server, encIdx);
            if (newkeys.cipherAuthLen() == 0)
                chooseMacAlg(newkeys, client, server, macIdx);
            chooseCompAlg(newkeys, client, server, compIdx);

            logger.fine(() -> String.format("KEX algo match; kex='%s', cipher='%s', MAC='%s', compression='%s', direction='%s', side='%s'",
                    kexName.name(),
                    newkeys.cipherSpec.name(),
                    newkeys.macSpec.name(),
                    "none", // FIXME
                    LoggerHelper.formatSideStr(c2s),
                    side
            ));
        }
    }

    private void chooseHostKeyAlg(KexInitEntries client, KexInitEntries server) throws TransportLayerException {
        hostKeyAlgName = chooseAlg(client, server, KexInitEntries.ENTRY_SERVER_HOST_KEY_ALG, "No matching HostKey algorithm");
        hostHostKeyAlgorithmSpec = HostKeyAlgorithmRegistry.byNameWithId(hostKeyAlgName);
        if (hostHostKeyAlgorithmSpec == null) {
            sendDisconnectMsg(Constant.SSH_DISCONNECT_KEY_EXCHANGE_FAILED, "Internal error, Negotiated host key algorithm is not supported");
            throw new KexException(String.format("Negotiated host key algorithm is not supported; algo='%s'", hostKeyAlgName.name()));
        }
    }

    private void chooseKex(KexInitEntries client, KexInitEntries server) throws TransportLayerException {
        kexName = chooseAlg(client, server, KexInitEntries.ENTRY_KEX_ALGOS, "No matching KEX algorithm");
        kexAlgorithmSpec = KexAlgorithmRegistry.byNameWithId(kexName);
        if (kexAlgorithmSpec == null) {
            sendDisconnectMsg(Constant.SSH_DISCONNECT_KEY_EXCHANGE_FAILED, "Internal error, Negotiated KEX algorithm is not supported");
            throw new KexException(String.format("v; algo='%s'", kexName.name()));
        }
    }

    private NameWithId chooseAlg(KexInitEntries client, KexInitEntries server, int index, String exceptionString) throws
            KexException {
        return matchList(client.entries[index], server.entries[index], exceptionString);
    }

    private NameWithId matchList(NameListWithIds client, NameListWithIds server, String exceptionString) throws
            KexException {
        int nameId = server.getFirstMatchingId(client);
        if (nameId == Name.SSH_NAME_UNKNOWN) {
            logger.severe(() -> String.format("KEX algo list mismatch; error='%s', own='%s', peer='%s', side='%s'",
                    exceptionString,
                    side.isClient() ? server.nameList() : client.nameList(),
                    side.isServer() ? server.nameList() : client.nameList(),
                    side
            ));
            throw new KexException(exceptionString);
        }

        return new NameWithId(nameId);
    }

    private void chooseEncAlg(NewKeys newKeys, KexInitEntries client, KexInitEntries server, int index) throws
            KexException {
        NameWithId encAlg = chooseAlg(client, server, index, "No matching Encryption algorithm");
        newKeys.setEncryption(encAlg);
    }

    private void chooseMacAlg(NewKeys newKeys, KexInitEntries client, KexInitEntries server, int index) throws
            KexException {
        NameWithId macAlg = chooseAlg(client, server, index, "No matching MAC algorithm");
        newKeys.setMac(macAlg);
    }

    private void chooseCompAlg(NewKeys newKeys, KexInitEntries client, KexInitEntries server, int index) throws
            KexException {
        NameWithId compAlg = chooseAlg(client, server, index, "No matching Compression algorithm");
        newKeys.setCompression(compAlg);
    }

    private void prepareDH() {
        if (kexState.getPeerInitPacket().follows && (
                !kexState.getPeerInitPacket().isFirstNameEquals(KexInitEntries.ENTRY_KEX_ALGOS, kexState.getOwnInitPacket())
                        || !kexState.getPeerInitPacket().isFirstNameEquals(KexInitEntries.ENTRY_SERVER_HOST_KEY_ALG, kexState.getOwnInitPacket()))) {
            this.transportLayer().skipPacket(Constant.SSH_MSG_KEXDH_INIT);
        }
    }

    public void registerNewKeysHandler() {
        transportLayer().getPacketHandlerRegistry().registerHandler(Constant.SSH_MSG_NEWKEYS, this::newKeysHandler);
    }

    public void newKeysHandler(Packet packet) {
        transportLayer().unregisterHandler(Constant.SSH_MSG_NEWKEYS);
    }

    public KexOutput getKexOutput() {
        return kexOutput;
    }

    protected void setKexOutput(KexOutput kexOutput) {
        this.kexOutput = kexOutput;
    }

    /*
    public enum State {
        INITIAL,
        INITIAL_KEX_INIT_SENT,
        WAIT_FOR_OTHER_KEXINIT, // This side already sent a KEXINIT - as per RFC 2453 7.1
        DROP_GUESSED_PACKET,    // the next packet should be dropped
        AFTER_KEX,
        KEX,
    }
    */

    protected static class KexState {
        protected final KexInitPacket ownInitPacket = new KexInitPacket();

        public KexInitPacket getOwnInitPacket() {
            return ownInitPacket;
        }

        protected KexInitPacket peerInitPacket;

        public KexInitPacket getPeerInitPacket() {
            return peerInitPacket;
        }

        public void setPeerInitPacket(KexInitPacket peerInitPacket) {
            this.peerInitPacket = peerInitPacket;
        }

        protected byte[] ownKexInit;

        public byte[] getOwnKexInit() {
            return ownKexInit;
        }

        public void setOwnKexInit(byte[] ownKexInit) {
            this.ownKexInit = ownKexInit;
        }

        protected byte[] peerKexInit;

        public byte[] getPeerKexInit() {
            return peerKexInit;
        }

        public void setPeerKexInit(byte[] peerKexInit) {
            this.peerKexInit = peerKexInit;
        }

        public KexState(Options.SideOptions options) {
            try {
                ownInitPacket.set(KexInitEntries.ENTRY_KEX_ALGOS, NameListWithIds.create(options.kexAlgorithms));
                ownInitPacket.set(KexInitEntries.ENTRY_SERVER_HOST_KEY_ALG, NameListWithIds.create(options.hostkeyAlgorithms));
                ownInitPacket.set(KexInitEntries.ENTRY_ENC_ALGOS_C2S, NameListWithIds.create(options.encryptionAlgorithms));
                ownInitPacket.set(KexInitEntries.ENTRY_ENC_ALGOS_S2C, ownInitPacket.get(KexInitEntries.ENTRY_ENC_ALGOS_C2S));
                ownInitPacket.set(KexInitEntries.ENTRY_MAC_ALGOS_C2S, NameListWithIds.create(options.macAlgorithms));
                ownInitPacket.set(KexInitEntries.ENTRY_MAC_ALGOS_S2C, ownInitPacket.get(KexInitEntries.ENTRY_MAC_ALGOS_C2S));
                ownInitPacket.set(KexInitEntries.ENTRY_COMP_ALGOS_C2S, NameListWithIds.create(options.compressionAlgorithms));
                ownInitPacket.set(KexInitEntries.ENTRY_COMP_ALGOS_S2C, ownInitPacket.get(KexInitEntries.ENTRY_COMP_ALGOS_C2S));
            } catch (KexException e) {
                // cannot happen. FIXME.
            }
        }
    }
}
