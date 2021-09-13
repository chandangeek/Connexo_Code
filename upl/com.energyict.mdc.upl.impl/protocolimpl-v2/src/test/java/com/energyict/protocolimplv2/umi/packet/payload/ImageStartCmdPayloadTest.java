package com.energyict.protocolimplv2.umi.packet.payload;

import com.energyict.protocolimplv2.umi.util.Limits;
import org.junit.Test;

import java.security.InvalidParameterException;

import static org.junit.Assert.assertEquals;

public class ImageStartCmdPayloadTest {
    @Test
    public void createImageStartCmdPayload() {
        long imageLength = 12;
        ImageStartCmdPayload payload = new ImageStartCmdPayload(imageLength);
        assertEquals(imageLength, payload.getImageLength());
        assertEquals(ImageStartCmdPayload.SIZE, payload.getLength());
    }

    @Test
    public void recreateImageStartCmdPayloadFromRaw() {
        long imageLength = 12;
        ImageStartCmdPayload payload = new ImageStartCmdPayload(imageLength);
        ImageStartCmdPayload payload1 = new ImageStartCmdPayload(imageLength);
        assertEquals(payload.getImageLength(), payload1.getImageLength());
        assertEquals(ImageStartCmdPayload.SIZE, payload1.getLength());
    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnInvalidRawData() {
        new ImageStartCmdPayload(new byte[0]);
    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnInvalidLength1() {
        new ImageStartCmdPayload(Limits.MAX_UNSIGNED_INT + 1);
    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnInvalidLength2() {
        new ImageStartCmdPayload(0);
    }
} 
