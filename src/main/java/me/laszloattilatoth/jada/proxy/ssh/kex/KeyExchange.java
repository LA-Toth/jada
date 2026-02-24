/*
 * Copyright 2020-2026 Laszlo Attila Toth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.laszloattilatoth.jada.proxy.ssh.kex;

import me.laszloattilatoth.jada.proxy.ssh.Options;
import me.laszloattilatoth.jada.proxy.ssh.algo.KeyAlgo;
import me.laszloattilatoth.jada.proxy.ssh.algo.KeyAlgos;
import me.laszloattilatoth.jada.proxy.ssh.core.Constant;
import me.laszloattilatoth.jada.proxy.ssh.core.Name;
import me.laszloattilatoth.jada.proxy.ssh.core.NameListWithIds;
import me.laszloattilatoth.jada.proxy.ssh.core.NameWithId;
import me.laszloattilatoth.jada.proxy.ssh.helpers.LoggerHelper;
import me.laszloattilatoth.jada.proxy.ssh.kex.algo.KexAlgo;
import me.laszloattilatoth.jada.proxy.ssh.kex.algo.KexAlgos;
import me.laszloattilatoth.jada.proxy.ssh.kex.dh.mina.AbstractDHKeyExchange;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.Packet;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.TransportLayer;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.TransportLayerException;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.WithTransportLayer;

import java.io.IOException;
import java.security.KeyPair;

public abstract class KeyExchange extends WithTransportLayer {
    protected final KexInitPacket ownInitPacket = new KexInitPacket();
    protected final NewKeys[] newKeys = new NewKeys[Constant.MODE_MAX];
    protected State state = State.INITIAL;
    protected KexInitPacket peerInitPacket;
    protected NameWithId kexName;
    protected NameWithId hostKeyAlgName;
    protected KexAlgo kexAlgo = null;
    protected KeyAlgo hostKeyAlgo = null;
    protected KeyPair hostKey;
    protected AbstractDHKeyExchange dhKex;
    protected byte[] ownKexInit;
    protected byte[] peerKexInit;

    private KexOutput kexOutput;

    public KeyExchange(TransportLayer transportLayer) {
        super(transportLayer);
        Options.SideOptions options = transportLayer.proxy().options().sideOptions(side);

        try {
            this.ownInitPacket.set(KexInitEntries.ENTRY_KEX_ALGOS, NameListWithIds.create(options.kexAlgorithms));
            this.ownInitPacket.set(KexInitEntries.ENTRY_SERVER_HOST_KEY_ALG, NameListWithIds.create(options.hostkeyAlgorithms));
            this.ownInitPacket.set(KexInitEntries.ENTRY_ENC_ALGOS_C2S, NameListWithIds.create(options.encryptionAlgorithms));
            this.ownInitPacket.set(KexInitEntries.ENTRY_ENC_ALGOS_S2C, this.ownInitPacket.get(KexInitEntries.ENTRY_ENC_ALGOS_C2S));
            this.ownInitPacket.set(KexInitEntries.ENTRY_MAC_ALGOS_C2S, NameListWithIds.create(options.macAlgorithms));
            this.ownInitPacket.set(KexInitEntries.ENTRY_MAC_ALGOS_S2C, this.ownInitPacket.get(KexInitEntries.ENTRY_MAC_ALGOS_C2S));
            this.ownInitPacket.set(KexInitEntries.ENTRY_COMP_ALGOS_C2S, NameListWithIds.create(options.compressionAlgorithms));
            this.ownInitPacket.set(KexInitEntries.ENTRY_COMP_ALGOS_S2C, this.ownInitPacket.get(KexInitEntries.ENTRY_COMP_ALGOS_C2S));
        } catch (KexException e) {
            // cannot happen. FIXME.
        }
    }

    public NewKeys clientNewKeys() {
        return side.isClient() ? this.newKeys[Constant.MODE_IN] : this.newKeys[Constant.MODE_OUT];
    }

    public NewKeys serverNewKeys() {
        return side.isServer() ? this.newKeys[Constant.MODE_IN] : this.newKeys[Constant.MODE_OUT];
    }

    public State getState() {
        return state;
    }

    public KeyPair getHostKey() {
        return hostKey;
    }

    public NameWithId hostKeyAlgName() {
        return hostKeyAlgName;
    }

    public void sendInitialMsgKexInit() throws KexException {
        if (ownKexInit == null) {
            Packet pkt = new Packet();
            ownInitPacket.writeToPacket(pkt);
            ownKexInit = pkt.getCompactData();
        }

        try {
            transportLayer().writePacket(ownKexInit, ownKexInit.length);
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
        peerInitPacket = initPacket;
        peerKexInit = packet.getCompactArray();
        chooseAlgos();
        prepareDH();
    }

    // Validated/partially based on OpenSSH kex.c: kex_choose_conf
    private void chooseAlgos() throws TransportLayerException {
        KexInitEntries client = side.isClient() ? peerInitPacket : ownInitPacket;
        KexInitEntries server = side.isClient() ? ownInitPacket : peerInitPacket;

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
                    newkeys.enc.name(),
                    newkeys.mac.name(),
                    "none", // FIXME
                    LoggerHelper.formatSideStr(c2s),
                    side
            ));
        }
    }

    private void chooseHostKeyAlg(KexInitEntries client, KexInitEntries server) throws TransportLayerException {
        hostKeyAlgName = chooseAlg(client, server, KexInitEntries.ENTRY_SERVER_HOST_KEY_ALG, "No matching HostKey algorithm");
        hostKeyAlgo = KeyAlgos.byNameWithId(hostKeyAlgName);
        if (hostKeyAlgo == null) {
            sendDisconnectMsg(Constant.SSH_DISCONNECT_KEY_EXCHANGE_FAILED, "Internal error, Negotiated host key algorithm is not supported");
            throw new KexException(String.format("Negotiated host key algorithm is not supported; algo='%s'", hostKeyAlgName.name()));
        }
    }

    private void chooseKex(KexInitEntries client, KexInitEntries server) throws TransportLayerException {
        kexName = chooseAlg(client, server, KexInitEntries.ENTRY_KEX_ALGOS, "No matching KEX algorithm");
        kexAlgo = KexAlgos.byNameWithId(kexName);
        if (kexAlgo == null) {
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

    private void prepareDH() throws TransportLayerException {
        if (peerInitPacket.follows && (
                !peerInitPacket.isFirstNameEquals(KexInitEntries.ENTRY_KEX_ALGOS, ownInitPacket)
                        || !peerInitPacket.isFirstNameEquals(KexInitEntries.ENTRY_SERVER_HOST_KEY_ALG, ownInitPacket))) {
            this.transportLayer().skipPacket(Constant.SSH_MSG_KEXDH_INIT);
        }
    }

    public void registerNewKeysHandler() {
        transportLayer().registerHandler(Constant.SSH_MSG_NEWKEYS, this::newKeysHandler);
    }

    public void newKeysHandler(Packet packet) throws TransportLayerException {
        transportLayer().unregisterHandler(Constant.SSH_MSG_NEWKEYS);
    }

    public KexOutput getKexOutput() {
        return kexOutput;
    }

    protected void setKexOutput(KexOutput kexOutput) {
        this.kexOutput = kexOutput;
    }

    public enum State {
        INITIAL,
        INITIAL_KEX_INIT_SENT,
        WAIT_FOR_OTHER_KEXINIT, // This side already sent a KEXINIT - as per RFC 2453 7.1
        DROP_GUESSED_PACKET,    // the next packet should be dropped
        AFTER_KEX,
        KEX,
    }
}
