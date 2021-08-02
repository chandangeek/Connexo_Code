package com.energyict.protocolimplv2.umi.packet.payload;

import org.junit.Test;

import java.security.InvalidParameterException;

import static org.junit.Assert.*;

public class ImageDataCmdPayloadTest {

    @Test
    public void createImageDataCmdPayload() {
        long sequenceNumber = 12;
        byte data[] = new byte[]{1,2, 3, 4, 5};
        ImageDataCmdPayload payload = new ImageDataCmdPayload(sequenceNumber, data);
        assertEquals(sequenceNumber, payload.getSequenceNumber());
        assertArrayEquals(data, payload.getData());
        assertTrue(ImageDataCmdPayload.MIN_SIZE < payload.getLength());
    }

    @Test
    public void recreateImageDataCmdPayloadFromRaw() {
        long sequenceNumber = 12;
        byte data[] = new byte[]{1,2, 3, 4, 5};
        ImageDataCmdPayload payload = new ImageDataCmdPayload(sequenceNumber, data);
        ImageDataCmdPayload payload1 = new ImageDataCmdPayload(payload.getRaw());
        assertEquals(sequenceNumber, payload1.getSequenceNumber());
        assertArrayEquals(data, payload1.getData());
        assertEquals(payload.getLength(), payload1.getLength());
    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnInvalidRawData() {
        new ImageDataCmdPayload(new byte[0]);
    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnInvalidData() {
        long sequenceNumber = 12;
        byte data[] = new byte[0];
        new ImageDataCmdPayload(sequenceNumber, data);
    }
} 
