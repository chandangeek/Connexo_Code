package com.energyict.protocolimplv2.umi.packet;

import com.energyict.protocolimplv2.umi.packet.payload.EvtPublishCmdPayload;
import com.energyict.protocolimplv2.umi.signature.scheme2.CmdSignatureS2;
import com.energyict.protocolimplv2.umi.types.Role;
import org.junit.Test;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class AppLayerPacketTest {
    static public CmdSignatureS2 createTestSignature() {
        Role role = Role.PERIPHERAL;
        Date from = new Date(123);
        Date until = new Date(1234);
        byte[] digitalSignature = new byte[CmdSignatureS2.MIN_DIGITAL_SIGNATURE_SIZE];
        new Random().nextBytes(digitalSignature);
        return new CmdSignatureS2(role, from, until, digitalSignature);
    }

    static public EvtPublishCmdPayload createTestPayload() {
        int eventBitfield = 0xFF;
        return new EvtPublishCmdPayload(eventBitfield);
    }

    static public AppLayerPacket createTestPacket() {
        AdditionalAuthenticatedData aad = AdditionalAuthenticatedDataTest.createTestAAD();
        EvtPublishCmdPayload payload = createTestPayload();
        HeaderPayloadData headerPayloadData = HeaderPayloadDataTest.createTestHeaderPayloadData(payload);
        CmdSignatureS2 cmdSignature = createTestSignature();
        AppLayerPacket appLayerPacket = new AppLayerPacket(aad, headerPayloadData, payload);
        appLayerPacket.setSignature(cmdSignature);
        return appLayerPacket;
    }

    @Test
    public void createPacketAndCheckFields() {
        AdditionalAuthenticatedData aad = AdditionalAuthenticatedDataTest.createTestAAD();
        EvtPublishCmdPayload payload = createTestPayload();
        HeaderPayloadData headerPayloadData = HeaderPayloadDataTest.createTestHeaderPayloadData(payload);
        CmdSignatureS2 cmdSignature = createTestSignature();
        AppLayerPacket appLayerPacket = new AppLayerPacket(aad, headerPayloadData, payload);
        appLayerPacket.setSignature(cmdSignature);

        int expectedLength = aad.getLength() + headerPayloadData.getLength() + payload.getLength() + cmdSignature.getLength();
        assertEquals(expectedLength, appLayerPacket.getLength());
        assertEquals(aad, appLayerPacket.getAdditionalAuthData());
        assertEquals(headerPayloadData, appLayerPacket.getHeaderPayloadData());
        assertEquals(payload, appLayerPacket.getPayload());
        assertEquals(cmdSignature, appLayerPacket.getSignature());
    }

    @Test
    public void recreatePacketFromRawData() {
        AppLayerPacket appLayerPacket = createTestPacket();

        byte[] rawPacket = appLayerPacket.getRaw();
        byte[] rawPacketData = Arrays.copyOfRange(rawPacket, AdditionalAuthenticatedData.SIZE, rawPacket.length);

        AdditionalAuthenticatedData aad2 = new AdditionalAuthenticatedData(Arrays.copyOfRange(rawPacket, 0, AdditionalAuthenticatedData.SIZE));
        AppLayerPacket appLayerPacket2 = new AppLayerPacket(aad2, rawPacketData);
        appLayerPacket.equals(appLayerPacket2);
        assertEquals(appLayerPacket, appLayerPacket2);
        assertArrayEquals(appLayerPacket.getRaw(), appLayerPacket2.getRaw());
    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnInitFromRawData() {
        new AppLayerPacket(AdditionalAuthenticatedDataTest.createTestAAD(), new byte[1]);
    }
}
