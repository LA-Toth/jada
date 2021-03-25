/*
 * Copyright 2020-2021 Laszlo Attila Toth
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
import me.laszloattilatoth.jada.proxy.ssh.core.*;
import me.laszloattilatoth.jada.proxy.ssh.kex.algo.KexAlgo;
import me.laszloattilatoth.jada.proxy.ssh.kex.algo.KexAlgos;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.Packet;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.TransportLayer;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.TransportLayerException;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.WithTransportLayer;

import java.io.IOException;
import java.util.Objects;

public class KeyExchange extends WithTransportLayer {
    private final State state = State.INITIAL;
    private final KexInitPacket ownInitPacket = new KexInitPacket();
    private final NewKeys[] newKeys = new NewKeys[Constant.MODE_MAX];
    private KexInitPacket peerInitPacket;
    private NameWithId kexName;
    private NameWithId hostKeyAlg;

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

    public State getState() {
        return state;
    }

    public boolean isClientSide() {
        return side == Constant.CLIENT_SIDE;
    }

    public boolean isServerSide() {
        return side == Constant.SERVER_SIDE;
    }

    public String sideStr() {
        return isClientSide() ? "client" : "server";
    }

    public void sendInitialMsgKexInit() throws KexException {
        Packet packet = new Packet();
        try {
            ownInitPacket.writeToPacket(packet);
        } catch (Packet.BufferEndReachedException e) {
            logger.severe(() -> String.format("Unable to send SSH_MSG_KEXINIT; error='%s'", e.getMessage()));
            throw new KexException(e.getMessage());
        }

        try {
            Objects.requireNonNull(transportLayer.get()).writePacket(packet);
        } catch (IOException e) {
            logger.severe(() -> String.format("Unable to send SSH_MSG_KEXINIT; error='%s'", e.getMessage()));
            throw new KexException(e.getMessage());
        }
    }

    /// RFC 4253 7.1.  Algorithm Negotiation (SSH_MSG_KEXINIT)
    public void processMsgKexInit(Packet packet) throws TransportLayerException {
        Objects.requireNonNull(transportLayer.get()).unregisterHandler(Constant.SSH_MSG_KEXINIT);

        KexInitPacket initPacket = new KexInitPacket();
        try {
            initPacket.readFromPacket(packet);
        } catch (Buffer.BufferEndReachedException e) {
            logger.severe(() -> String.format("Unable to parse SSH_MSG_KEXINIT; error='%s'", e.getMessage()));
            throw new KexException(e.getMessage());
        }

        if (!initPacket.valid()) {
            logger.severe("Peer KEXINIT packet contains at least algorithm list which is empty or contains only unknown algos;");
            throw new KexException("Unable to process SSH_MSG_KEXINIT, no known algorithms;");
        }

        // TODO: rekeying -send our own kex packet
        // TODO: not always save peer packet
        peerInitPacket = initPacket;
        chooseAlgos();
    }

    // Validated/partially based on OpenSSH kex.c: kex_choose_conf
    private void chooseAlgos() throws TransportLayerException {
        KexInitEntries client = isClientSide() ? peerInitPacket : ownInitPacket;
        KexInitEntries server = isClientSide() ? ownInitPacket : peerInitPacket;

        // not checking ext_info_c - RFC 8308

        // Choose all algos one by one, and throw exception if no matching algo
        chooseKex(client, server);
        chooseHostKeyAlg(client, server);

        for (int mode = Constant.MODE_IN; mode != Constant.MODE_MAX; ++mode) {
            boolean c2s = (isClientSide() && mode == Constant.MODE_IN) || (isServerSide() && mode == Constant.MODE_OUT);
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
                    kexName.getName(),
                    newkeys.enc.name(),
                    newkeys.mac.name(),
                    "none", // FIXME
                    c2s ? "client->server" : "server->client",
                    sideStr()
            ));
        }

        // Send KexDH reply
        //
        /*

        	need = dh_need = 0;
	for (mode = 0; mode < MODE_MAX; mode++) {
		newkeys = kex->newkeys[mode];
		need = MAXIMUM(need, newkeys->enc.key_len);
		need = MAXIMUM(need, newkeys->enc.block_size);
		need = MAXIMUM(need, newkeys->enc.iv_len);
		need = MAXIMUM(need, newkeys->mac.key_len);
		dh_need = MAXIMUM(dh_need, cipher_seclen(newkeys->enc.cipher));
		dh_need = MAXIMUM(dh_need, newkeys->enc.block_size);
		dh_need = MAXIMUM(dh_need, newkeys->enc.iv_len);
		dh_need = MAXIMUM(dh_need, newkeys->mac.key_len);
	}
        kex->we_need = need;
        kex->dh_need = dh_need;


        if (first_kex_follows && !proposals_match(my, peer))
            ssh->dispatch_skip_packets = 1;
        r = 0;
        out:
         */
    }

    private void chooseHostKeyAlg(KexInitEntries client, KexInitEntries server) throws TransportLayerException {
        hostKeyAlg = chooseAlg(client, server, KexInitEntries.ENTRY_SERVER_HOST_KEY_ALG, "No matching HostKey algorithm");
        KeyAlgo keyAlgo = KeyAlgos.byNameWithId(this.kexName);
        if (keyAlgo == null) {
            sendDisconnectMsg(Constant.SSH_DISCONNECT_KEY_EXCHANGE_FAILED, "Internal error, Negotiated host key algorithm is not supported");
            throw new KexException(String.format("Negotiated host key algorithm is not supported; algo='%s'", hostKeyAlg.getName()));
        }
    }

    private void chooseKex(KexInitEntries client, KexInitEntries server) throws TransportLayerException {
        this.kexName = chooseAlg(client, server, KexInitEntries.ENTRY_KEX_ALGOS, "No matching KEX algorithm");
        KexAlgo kexAlgo = KexAlgos.byNameWithId(this.kexName);
        if (kexAlgo == null) {
            sendDisconnectMsg(Constant.SSH_DISCONNECT_KEY_EXCHANGE_FAILED, "Internal error, Negotiated KEX algorithm is not supported");
            throw new KexException(String.format("v; algo='%s'", kexName.getName()));
        }
    }

    private NameWithId chooseAlg(KexInitEntries client, KexInitEntries server, int index, String exceptionString) throws KexException {
        return matchList(client.entries[index], server.entries[index], exceptionString);
    }

    private NameWithId matchList(NameListWithIds client, NameListWithIds server, String exceptionString) throws KexException {
        int nameId = server.getFirstMatchingId(client);
        if (nameId == Name.SSH_NAME_UNKNOWN) {
            logger.severe(() -> String.format("KEX algo list mismatch; error='%s', own='%s', peer='%s', side='%s'",
                    exceptionString,
                    isClientSide() ? server.getNameList() : client.getNameList(),
                    isServerSide() ? server.getNameList() : client.getNameList(),
                    sideStr()
            ));
            throw new KexException(exceptionString);
        }

        return new NameWithId(nameId);
    }

    private void chooseEncAlg(NewKeys newKeys, KexInitEntries client, KexInitEntries server, int index) throws KexException {
        NameWithId encAlg = chooseAlg(client, server, index, "No matching Encryption algorithm");
        newKeys.setEncryption(encAlg);
    }

    private void chooseMacAlg(NewKeys newKeys, KexInitEntries client, KexInitEntries server, int index) throws KexException {
        NameWithId macAlg = chooseAlg(client, server, index, "No matching MAC algorithm");
        newKeys.setMac(macAlg);
    }

    private void chooseCompAlg(NewKeys newKeys, KexInitEntries client, KexInitEntries server, int index) throws KexException {
        NameWithId compAlg = chooseAlg(client, server, index, "No matching Compression algorithm");
        newKeys.setCompression(compAlg);
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
