// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: Copyright (c) Laszlo Attila Toth

package me.laszloattilatoth.jada.proxy.ssh.transportlayer;

import me.laszloattilatoth.jada.core.TestRandom;
import me.laszloattilatoth.jada.proxy.ssh.kex.KexOutput;
import me.laszloattilatoth.jada.proxy.ssh.crypto.CipherSuite;
import me.laszloattilatoth.jada.proxy.ssh.crypto.SessionKeys;
import me.laszloattilatoth.jada.proxy.ssh.kex.algorithm.CipherRegistry;
import me.laszloattilatoth.jada.proxy.ssh.kex.algorithm.CipherSpec;
import me.laszloattilatoth.jada.util.logging.LoggerFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.*;

public class TransportLayerIOTest {

    // positive test case, where the padding is more than 4 bytes long, and ends at the "first" block
    // - the block that contains the 4th byte, which is the second block for a single byte packet
    @Test
    public void testClearTextRead() {
        TransportLayerInput subject = createTransportLayerIO();
        GrowingInputStream is = new GrowingInputStream();

        try {
            subject.setInputStream(is);
        } catch (TransportLayerException e) {
            fail(e.getMessage());
        }

        is.addInt(12);
        is.addByte((byte) 4);
        is.addByte((byte) 42); // payload
        is.addSecureBytes(6); // payload
        is.addSecureBytes(4); // padding

        Packet readPacket;
        try {
            readPacket = subject.readPacket();
        } catch (TransportLayerException e) {
            fail(e.getMessage());
            return; // already failed, make Java happy
        }

        assertEquals(7, readPacket.wpos());
        assertEquals(42, readPacket.packetType());
        try {
            assertEquals(0, is.available(), "not the whole data was read");
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void readClearTextPacketWithoutEndingOnBlockSizeFails() {
        TransportLayerInput subject = createTransportLayerIO();
        GrowingInputStream is = new GrowingInputStream();

        try {
            subject.setInputStream(is);
        } catch (TransportLayerException e) {
            fail(e.getMessage());
        }

        is.addInt(6);
        is.addByte((byte) 4);
        is.addByte((byte) 42);
        is.addSecureBytes(4);

        TransportLayerException exception = assertThrows(
                TransportLayerException.class,
                subject::readPacket,
                "Expected readPacket() to throw, but it didn't"
        );

        assertTrue(exception.getMessage().contains("Read packet size is not multiple of block size"));
    }

    @Test
    public void readClearTextPacketWithTooSmallPaddingFails() {
        TransportLayerInput subject = createTransportLayerIO();
        GrowingInputStream is = new GrowingInputStream();

        try {
            subject.setInputStream(is);
        } catch (TransportLayerException e) {
            fail(e.getMessage());
        }

        is.addInt(12);
        is.addByte((byte) 3);
        is.addByte((byte) 42);
        is.addSecureBytes(7);
        is.addSecureBytes(3);

        TransportLayerException exception = assertThrows(
                TransportLayerException.class,
                subject::readPacket,
                "Expected readPacket() to throw, but it didn't"
        );

        assertTrue(exception.getMessage().contains("Padding length is smaller than"));
    }

    @Test
    public void writeAndReadClearTextPacketWithOneByte() {
        TransportLayerInputOutput subject = createTransportLayerIO();
        GrowingInputStream is = new GrowingInputStream();
        try {
            subject.setInputStream(is);
            subject.setOutputStream(is.getOutputStream());
        } catch (TransportLayerException e) {
            fail(e.getMessage());
        }

        try {
            Packet packet = new Packet();
            packet.putByte((byte) 42);
            subject.writePacket(packet);
        } catch (IOException e) {
            fail(e.getMessage());
        }

        try {
            Packet packet = subject.readPacket();
            assertEquals(1, packet.wpos());
            assertEquals(42, packet.packetType());
        } catch (TransportLayerException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void writeAndReadClearTextPacketWithAlmostBlockSizeLength() {
        TransportLayerInputOutput subject = createTransportLayerIO();
        GrowingInputStream is = new GrowingInputStream();
        try {
            subject.setInputStream(is);
            subject.setOutputStream(is.getOutputStream());
        } catch (TransportLayerException e) {
            fail(e.getMessage());
        }

        try {
            Packet packet = new Packet();
            packet.putByte((byte) 42);
            packet.putInt(5);
            packet.putShort((short) 6);
            subject.writePacket(packet);
        } catch (IOException e) {
            fail(e.getMessage());
        }

        try {
            Packet packet = subject.readPacket();
            assertEquals(7, packet.wpos());
            assertEquals(42, packet.packetType());
            assertEquals(42, packet.getByte());
            assertEquals(0, packet.getByte());
            assertEquals(0, packet.getByte());
            assertEquals(5 * 256 * 256 + 6, packet.getInt());
        } catch (TransportLayerException e) {
            fail(e.getMessage());
        }
    }


    /// Teest the case when the writer is still in cleartext mode
    /// but the reader is prepared for encryption; and the change is not yet happened
    @Test
    public void writeAndReadClearTextPacketWithPreppedReaderEncryption() {
        TransportLayerOutput writer = createTransportLayerIO();
        TransportLayerInput reader = createTransportLayerIO();
        reader.addInboundSessionKeys(createNewKeysForEncryption());

        GrowingInputStream is = new GrowingInputStream();

        try {
            reader.setInputStream(is);
            writer.setOutputStream(is.getOutputStream());
        } catch (TransportLayerException e) {
            fail(e.getMessage());
        }

        try {
            Packet packet = new Packet();
            packet.putByte((byte) 42);
            writer.writePacket(packet);
        } catch (IOException e) {
            fail(e.getMessage());
        }

        try {
            Packet packet = reader.readPacket();
            assertEquals(1, packet.wpos());
            assertEquals(42, packet.packetType());
        } catch (TransportLayerException e) {
            fail(e.getMessage());
        }
    }

    /// Teest the case when the reader is still in cleartext mode
    /// but the writer is encrypting
    @Test
    public void readEncryptedPacketWithCleartextReaderFails() {
        TransportLayerOutput writer = createTransportLayerIO();
        TransportLayerInput reader = createTransportLayerIO();
        writer.addSenderNewKeys(createNewKeysForEncryption());
        writer.sshMsgNewKeysSent();

        GrowingInputStream is = new GrowingInputStream();

        try {
            reader.setInputStream(is);
            writer.setOutputStream(is.getOutputStream());
        } catch (TransportLayerException e) {
            fail(e.getMessage());
        }

        try {
            Packet packet = new Packet();
            packet.putByte((byte) 42);
            writer.writePacket(packet);
        } catch (IOException e) {
            fail(e.getMessage());
        }

        TransportLayerException exception = assertThrows(
                TransportLayerException.class,
                reader::readPacket,
                "Expected readPacket() to throw, but it didn't"
        );


        // TODO: update this
        assertTrue(exception.getMessage().contains("Padding length is smaller than"));
    }

    void writeAndReadEncryptedPacketIsSuccessful() {
        // TODO
    }

    private TransportLayerIO createTransportLayerIO() {
        return new TransportLayerIO(LoggerFactory.getNulLogger("test"));
    }

    private CipherSuite createNewKeysForEncryption() {
        CipherSpec cipherSpec = CipherRegistry.byName("aes128-ctr");

        KexOutput output = createKexOutput(cipherSpec);

        SessionKeys sessionKeys = SessionKeys.createClientSessionKeys(output); // server's keys are the same here

        CipherSuite newKeys = new CipherSuite();
        newKeys.cipherSpec = cipherSpec;
        newKeys.sessionKeys = sessionKeys;


        return newKeys;
    }


    private KexOutput createKexOutput(CipherSpec cipherSpec) {
        SecureRandom secureRandom = new TestRandom(0x22deada458beefL);
        byte[] iv = new byte[cipherSpec.ivLen()];
        byte[] enc_key = new byte[cipherSpec.keyLen()];

        // TODO: actually implement this
        byte[] mac_key = new byte[42];

        secureRandom.nextBytes(iv);
        secureRandom.nextBytes(enc_key);
        secureRandom.nextBytes(mac_key);

        return new KexOutput(iv, iv, enc_key, enc_key, mac_key, mac_key);
    }
}
