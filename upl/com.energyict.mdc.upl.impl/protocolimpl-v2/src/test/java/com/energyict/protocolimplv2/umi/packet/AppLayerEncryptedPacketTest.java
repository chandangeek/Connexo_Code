package com.energyict.protocolimplv2.umi.packet;

import org.junit.Test;

import java.security.InvalidParameterException;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class AppLayerEncryptedPacketTest {
    @Test
    public void createPacketAndCheckFields() {
        AppLayerPacket appLayerPacket = AppLayerPacketTest.createTestPacket();
        byte[] rawPacket = appLayerPacket.getRaw();
        byte[] rawPacketData = Arrays.copyOfRange(rawPacket, AdditionalAuthenticatedData.SIZE, rawPacket.length);
        AppLayerEncryptedPacket appLayerEncryptedPacket = new AppLayerEncryptedPacket(appLayerPacket.getAdditionalAuthData(), rawPacketData);

        int expectedLength = rawPacket.length;
        assertEquals(expectedLength, appLayerPacket.getLength());

        assertEquals(appLayerPacket.getAdditionalAuthData(), appLayerEncryptedPacket.getAdditionalAuthData());
        assertArrayEquals(rawPacketData, appLayerEncryptedPacket.getCipherText());
    }

    @Test
    public void recreatePacketFromRawData() {
        AppLayerPacket appLayerPacket = AppLayerPacketTest.createTestPacket();
        byte[] rawPacket = appLayerPacket.getRaw();
        byte[] rawPacketData = Arrays.copyOfRange(rawPacket, AdditionalAuthenticatedData.SIZE, rawPacket.length);
        AppLayerEncryptedPacket appLayerEncryptedPacket = new AppLayerEncryptedPacket(appLayerPacket.getAdditionalAuthData(), rawPacketData);

        AppLayerEncryptedPacket appLayerEncryptedPacket2 = new AppLayerEncryptedPacket(appLayerEncryptedPacket.getRaw());
        assertEquals(appLayerEncryptedPacket, appLayerEncryptedPacket2);
    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnInitFromRawData() {
        new AppLayerEncryptedPacket(new byte[1]);
    }
}
