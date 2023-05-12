package com.energyict.protocolimplv2.umi.security;

import com.energyict.protocolimplv2.umi.ei4.Keys;
import com.energyict.protocolimplv2.umi.ei4.structures.UmiManufacturerId;
import com.energyict.protocolimplv2.umi.packet.*;
import com.energyict.protocolimplv2.umi.packet.payload.EvtPublishCmdPayload;
import com.energyict.protocolimplv2.umi.properties.UmiPropertiesBuilder;
import com.energyict.protocolimplv2.umi.properties.UmiSessionPropertiesS2;
import com.energyict.protocolimplv2.umi.security.scheme2.AppPacketSecurityS2;
import com.energyict.protocolimplv2.umi.session.UmiSession;
import com.energyict.protocolimplv2.umi.session.UmiSessionS2Utils;
import com.energyict.protocolimplv2.umi.session.UmiSessionS2UtilsTest;
import com.energyict.protocolimplv2.umi.types.UmiId;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Before;
import org.junit.Test;

import java.security.Security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AppPacketSecurityS2Test {
    @Before
    public void before() {
        Security.addProvider(new BouncyCastleProvider());
    }

    static AppLayerPacket createTestPacket(SecurityScheme encryptionScheme, SecurityScheme signatureScheme,
                                           SecurityScheme respSignatureScheme) {
        AppPacketType type = AppPacketType.EVENT_PUBLISH;
        short transactionNumber = 9;

        byte encOptions = 10;
        byte sourceOptions = 11;
        byte destinationOptions = 12;
        UmiId destUmiId = new UmiId("123");
        UmiId sourceUmiId = new UmiId("456");

        AdditionalAuthenticatedData aad = new AdditionalAuthenticatedData.Builder()
                .encryptionScheme(encryptionScheme)
                .encryptionOptions(encOptions)
                .sourceOptions(sourceOptions)
                .destinationOptions(destinationOptions)
                .sourceUmiId(sourceUmiId)
                .destinationUmiId(destUmiId).build();

        EvtPublishCmdPayload payload = AppLayerPacketTest.createTestPayload();

        HeaderPayloadData headerPayloadData = new HeaderPayloadData.Builder()
                .packetType(type)
                .signatureScheme(signatureScheme)
                .respSignatureSchemeRequest(respSignatureScheme)
                .transactionNumber(transactionNumber)
                .payloadLength(payload)
                .build();
        return new AppLayerPacket(aad, headerPayloadData, payload);
    }

    @Test
    public void noSecurityEncryptionTest() throws Exception {
        UmiSessionPropertiesS2 session = new UmiSessionPropertiesS2(new UmiPropertiesBuilder()
                .cmdSignatureScheme(SecurityScheme.NO_SECURITY)
                .encryptionScheme(SecurityScheme.NO_SECURITY)
                .respSignatureSchemeRequest(SecurityScheme.NO_SECURITY)
                .sourceUmiId(new UmiId("0"))
                .destinationUmiId(new UmiId("1")),
                UmiSessionS2UtilsTest.getLocalCert());

        AppPacketSecurityS2 securityS2 = new AppPacketSecurityS2(session);
        AppLayerPacket packet = createTestPacket(SecurityScheme.NO_SECURITY, SecurityScheme.NO_SECURITY, SecurityScheme.NO_SECURITY);
        packet.getAdditionalAuthData().setEncryptionScheme(SecurityScheme.NO_SECURITY);
        AppLayerEncryptedPacket encryptedPacket = securityS2.encrypt(packet);
        AppLayerPacket decryptedPacket = securityS2.decrypt(encryptedPacket);

        assertEquals(packet, decryptedPacket);
    }

    @Test
    public void encryptionDecryptionTest() throws Exception {
        UmiSessionPropertiesS2 session = new UmiSessionPropertiesS2(new UmiPropertiesBuilder()
                .cmdSignatureScheme(SecurityScheme.NO_SECURITY)
                .encryptionScheme(SecurityScheme.ASYMMETRIC)
                .respSignatureSchemeRequest(SecurityScheme.NO_SECURITY)
                .sourceUmiId(new UmiId("9991253571665920"))
                .destinationUmiId(UmiSession.destinationModemUmiId)
                .ownPrivateKey(UmiSessionS2UtilsTest.getLocalKey())
                .ownCertificate(UmiSessionS2UtilsTest.getLocalCert()));;

        Security.addProvider(new BouncyCastleProvider());

        byte[] saltA = UmiSessionS2Utils.generateSalt();
        byte[] saltB = UmiSessionS2Utils.generateSalt();

        byte[] sessionKey = UmiSessionS2Utils.createSessionKey(UmiSessionS2UtilsTest.getLocalKey(),
                UmiSessionS2UtilsTest.getRemoteCert(), saltA, saltB);

        session.setSessionKey(sessionKey);
        session.setRemoteCertificate(UmiSessionS2UtilsTest.getRemoteCert());
        AppPacketSecurityS2 securityS2 = new AppPacketSecurityS2(session);
        AppLayerPacket packet = createTestPacket(SecurityScheme.ASYMMETRIC, SecurityScheme.NO_SECURITY, SecurityScheme.NO_SECURITY);
        AppLayerEncryptedPacket encryptedPacket = securityS2.encrypt(packet);
        session.getInboundIV().increment();
        session.getOutboundIV().getCounter()[0]--;
        encryptedPacket.getAdditionalAuthData().setEncryptionOptions((byte) session.getOutboundIV().getCounter()[0]);
        AppLayerPacket decryptedPacket = securityS2.decrypt(encryptedPacket);

        assertEquals(packet, decryptedPacket);
    }

    @Test
    public void decryptionRealTest() throws Exception {
        UmiSessionPropertiesS2 properties = new UmiSessionPropertiesS2(new UmiPropertiesBuilder()
                .cmdSignatureScheme(SecurityScheme.NO_SECURITY)
                .encryptionScheme(SecurityScheme.ASYMMETRIC)
                .respSignatureSchemeRequest(SecurityScheme.NO_SECURITY)
                .sourceUmiId(new UmiId("9991253571665924"))
                .destinationUmiId(new UmiId("9991253571665921"))
                .ownPrivateKey(UmiSessionS2UtilsTest.getLocalKey())
                .ownCertificate(UmiSessionS2UtilsTest.getLocalCert()));

        Security.addProvider(new BouncyCastleProvider());

        byte[] saltA = {(byte)0xda, (byte)0x79, (byte)0x6f, (byte)0x87, (byte)0x02, (byte)0x81, (byte)0x2b, (byte)0x56,
                (byte)0xbb, (byte)0x18, (byte)0x4a, (byte)0xdb, (byte)0xb1, (byte)0x76, (byte)0x40, (byte)0x4e};
        byte[] saltB = {(byte)0x22, (byte)0xcd, (byte)0xc8, (byte)0x9e, (byte)0x46, (byte)0x3d, (byte)0x15, (byte)0x3f,
                (byte)0x53, (byte)0xae, (byte)0x96, (byte)0x31, (byte)0xd7, (byte)0xd7, (byte)0xb0, (byte)0x4f};


        byte[] sessionKey = UmiSessionS2Utils.createSessionKey(UmiSessionS2UtilsTest.getLocalKey(),
                UmiSessionS2UtilsTest.getRemoteCertRole6(), saltA, saltB);

        properties.setSessionKey(sessionKey);
        properties.setRemoteCertificate(Keys.getRemoteCMCert());
        properties.setEstablished(true);

        AppPacketSecurityS2 securityS2 = new AppPacketSecurityS2(properties);

        byte[] encryptedResponse = {/*(byte)0x41, (byte)0x01, (byte)0x51, (byte)0x51,*/ (byte)0x02, (byte)0x01, (byte)0x00, (byte)0x00,
                (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xfe, (byte)0x7e, (byte)0x23, (byte)0x00,
                (byte)0x04, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xfe, (byte)0x7e, (byte)0x23, (byte)0x00,
                (byte)0x01, (byte)0xf3, (byte)0xdf, (byte)0x6b, (byte)0x0f, (byte)0xde, (byte)0x96, (byte)0x14,
                (byte)0xd5, (byte)0xf6, (byte)0x76, (byte)0x73, (byte)0x91, (byte)0x65, (byte)0x4d, (byte)0xdb,
                (byte)0x0f, (byte)0xda, (byte)0xca, (byte)0xc5, (byte)0xa8, (byte)0xf6, (byte)0x8e, (byte)0xa2,
                (byte)0xe7, (byte)0xe4, (byte)0x76, (byte)0xb5, (byte)0x11, (byte)0x09, (byte)0xca, (byte)0x7d,
                (byte)0x1f, (byte)0x3e, (byte)0xa2, (byte)0x4b, (byte)0x29, (byte)0x33, (byte)0x46, (byte)0x8a,
                (byte)0x9a, (byte)0xf1, (byte)0x82, (byte)0x92, (byte)0xf8, (byte)0x76, (byte)0x96, (byte)0x6b,
                (byte)0x23, (byte)0xbd, (byte)0xf4, (byte)0x17, (byte)0xa1, (byte)0x25, (byte)0x26, (byte)0x18,
                (byte)0x35, (byte)0x91, (byte)0x84, (byte)0x78, (byte)0x22, /*(byte)0xc1, (byte)0xc1*/};

        AppLayerEncryptedPacket encryptedPacket = new AppLayerEncryptedPacket(encryptedResponse);
        properties.getOutboundIV().increment();
        AppLayerPacket decryptedPacket = securityS2.decrypt(encryptedPacket);
        byte[] raw = decryptedPacket.getRaw();
        StringBuffer toLog = new StringBuffer("Received from device: ");
        for (int i = 0; i < raw.length; i++) {
            toLog.append(String.format("0x%02X", raw[i])).append(" ");
        }
        System.out.println(toLog);
        byte[] id = new byte[23];
        System.arraycopy(decryptedPacket.getPayload().getRaw(), 8, id, 0, 23);

        UmiManufacturerId umiManufacturerId = new UmiManufacturerId(id);
        assertEquals("78340915", umiManufacturerId.getSerialNumber());
    }

    @Test
    public void noSecuritySignTest() throws Exception {
        UmiSessionPropertiesS2 session = new UmiSessionPropertiesS2(new UmiPropertiesBuilder()
                .cmdSignatureScheme(SecurityScheme.NO_SECURITY)
                .encryptionScheme(SecurityScheme.NO_SECURITY)
                .respSignatureSchemeRequest(SecurityScheme.NO_SECURITY)
                .sourceUmiId(new UmiId("0"))
                .destinationUmiId(new UmiId("1")),
                UmiSessionS2UtilsTest.getLocalCert());

        AppPacketSecurityS2 securityS2 = new AppPacketSecurityS2(session);
        AppLayerPacket packet = createTestPacket(SecurityScheme.NO_SECURITY, SecurityScheme.NO_SECURITY, SecurityScheme.NO_SECURITY);
        packet.getAdditionalAuthData().setEncryptionScheme(SecurityScheme.NO_SECURITY);
        securityS2.sign(packet);
        securityS2.verifySignature(packet);
    }

    @Test
    public void signVerifyTest() throws Exception {
        UmiSessionPropertiesS2 session = new UmiSessionPropertiesS2(new UmiPropertiesBuilder()
                .cmdSignatureScheme(SecurityScheme.ASYMMETRIC)
                .encryptionScheme(SecurityScheme.NO_SECURITY)
                .respSignatureSchemeRequest(SecurityScheme.ASYMMETRIC)
                .sourceUmiId(new UmiId("0"))
                .destinationUmiId(new UmiId("1"))
                .ownPrivateKey(UmiSessionS2UtilsTest.getLocalKey())
                .ownCertificate(UmiSessionS2UtilsTest.getLocalCert()));

        Security.addProvider(new BouncyCastleProvider());

        byte[] saltA = UmiSessionS2Utils.generateSalt();
        byte[] saltB = UmiSessionS2Utils.generateSalt();

        byte[] sessionKey = UmiSessionS2Utils.createSessionKey(UmiSessionS2UtilsTest.getLocalKey(),
                UmiSessionS2UtilsTest.getRemoteCert(), saltA, saltB);

        session.setSessionKey(sessionKey);
        /** set remote certificate to local one to imitate response signature verification */
        session.setRemoteCertificate(UmiSessionS2UtilsTest.getLocalCert());

        AppPacketSecurityS2 securityS2 = new AppPacketSecurityS2(session);

        AppLayerPacket packet = createTestPacket(SecurityScheme.NO_SECURITY, SecurityScheme.ASYMMETRIC, SecurityScheme.ASYMMETRIC);

        securityS2.sign(packet);
        assertTrue(securityS2.verifySignature(packet));
    }
} 
