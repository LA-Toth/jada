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
import me.laszloattilatoth.jada.proxy.ssh.core.Constant;
import me.laszloattilatoth.jada.proxy.ssh.core.Name;
import me.laszloattilatoth.jada.proxy.ssh.core.NameListWithIds;
import me.laszloattilatoth.jada.proxy.ssh.core.NameWithId;
import me.laszloattilatoth.jada.proxy.ssh.kex.algo.KexAlgo;
import me.laszloattilatoth.jada.proxy.ssh.kex.algo.KexAlgos;
import me.laszloattilatoth.jada.proxy.ssh.kex.dh.DH;
import me.laszloattilatoth.jada.proxy.ssh.kex.dh.DHFactory;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.Packet;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.TransportLayer;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.TransportLayerException;
import me.laszloattilatoth.jada.proxy.ssh.transportlayer.WithTransportLayer;
import org.apache.sshd.common.util.buffer.ByteArrayBuffer;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

public class KeyExchange extends WithTransportLayer {
    private final State state = State.INITIAL;
    private final KexInitPacket ownInitPacket = new KexInitPacket();
    private final NewKeys[] newKeys = new NewKeys[Constant.MODE_MAX];
    protected AsymmetricCipherKeyPair hostKey;
    DH dh;
    private KexInitPacket peerInitPacket;
    private NameWithId kexName;
    private NameWithId hostKeyAlgName;
    private KexAlgo kexAlgo = null;
    private KeyAlgo hostKeyAlgo = null;
    private byte[] ownKexInit;
    private byte[] peerKexInit;

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

    private void loadHostKey() throws TransportLayerException {
        try {
            AsymmetricKeyParameter privateKey = readPrivateKey(Paths.get(System.getProperty("user.home"), ".config/jada/ssh_host_rsa_key").toFile());

            if (privateKey instanceof RSAPrivateCrtKeyParameters rsa) {
                var pub = new RSAKeyParameters(false, rsa.getModulus(), rsa.getPublicExponent());
                hostKey = new AsymmetricCipherKeyPair(pub, privateKey);
            } else if (privateKey instanceof Ed25519PrivateKeyParameters ed) {
                var pub = ed.generatePublicKey();
                hostKey = new AsymmetricCipherKeyPair(pub, privateKey);
            }
            if (hostKey == null)
                throw new TransportLayerException(String.format("The key type %s is not supported.", privateKey));
        } catch (IOException e) {
            throw new TransportLayerException(e.getMessage());
        }
    }

    protected AsymmetricKeyParameter readPrivateKey(File file) throws IOException, TransportLayerException {
        try (FileReader keyReader = new FileReader(file)) {
            PEMParser pemParser = new PEMParser(keyReader);
            PEMKeyPair keyPair = (PEMKeyPair) pemParser.readObject();
            PrivateKeyInfo pki = keyPair.getPrivateKeyInfo();
            try {
                return PrivateKeyFactory.createKey(pki);
            } catch (IOException ex) {
                throw new TransportLayerException(ex.getMessage());
            }
        }
    }

    public State getState() {
        return state;
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
        peerKexInit = packet.getCompactData();
        chooseAlgos();
        prepareDH(initPacket);
        transportLayer().registerHandler(Constant.SSH_MSG_KEXDH_INIT, this::processKexDhInit, "SSH_MSG_KEXDH_INIT");
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
                    c2s ? "client->server" : "server->client",
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

    private NameWithId chooseAlg(KexInitEntries client, KexInitEntries server, int index, String exceptionString) throws KexException {
        return matchList(client.entries[index], server.entries[index], exceptionString);
    }

    private NameWithId matchList(NameListWithIds client, NameListWithIds server, String exceptionString) throws KexException {
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

    private void prepareDH(KexInitPacket initPacket) throws TransportLayerException {
        if (initPacket.follows && (
                !initPacket.isFirstNameEquals(KexInitEntries.ENTRY_KEX_ALGOS, ownInitPacket)
                        || !initPacket.isFirstNameEquals(KexInitEntries.ENTRY_SERVER_HOST_KEY_ALG, ownInitPacket))) {
            this.transportLayer().skipPackets(1);
        }
        loadHostKey();
    }

    public void processKexDhInit(Packet packet) throws TransportLayerException {
        byte[] e = null;
        packet.getByte();
        e = packet.getMPIntAsBytes();
        if (!packet.endReached())
            throw new TransportLayerException("Unexpected additional data found in Packet");

        dh = DHFactory.createFromKexAlgoId(kexAlgo.nameId(),
                Constant.SSH_ID_STRING,
                transportLayer().peerIDString(),
                ownKexInit,
                peerKexInit,
                side
        );

        dh.setE(e);

        ByteArrayBuffer buffer = new ByteArrayBuffer();
        //buffer.putPublicKey(hostKey.getPublic());
        byte[] k_s = buffer.getCompactData();
        buffer = dh.prepareBuffer();
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
