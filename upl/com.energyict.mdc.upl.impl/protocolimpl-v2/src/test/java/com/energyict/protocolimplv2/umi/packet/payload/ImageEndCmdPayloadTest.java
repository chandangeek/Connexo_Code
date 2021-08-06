package com.energyict.protocolimplv2.umi.packet.payload;

import org.junit.Test;

import java.security.InvalidParameterException;

import static org.junit.Assert.assertEquals;

public class ImageEndCmdPayloadTest {
    @Test
    public void createImageEndCmdPayload() {
        boolean valid = false;
        ImageEndCmdPayload payload = new ImageEndCmdPayload(valid);
        assertEquals(valid, payload.getValid());
        assertEquals(ImageEndCmdPayload.SIZE, payload.getLength());
    }

    @Test
    public void recreateImageEndCmdPayloadFromRaw() {
        ImageEndCmdPayload payload = new ImageEndCmdPayload(true);
        ImageEndCmdPayload payload1 = new ImageEndCmdPayload(payload.getRaw());
        assertEquals(payload.getValid(), payload1.getValid());
        assertEquals(ImageEndCmdPayload.SIZE, payload.getLength());
    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnInvalidRawData() {
        new ImageEndCmdPayload(new byte[0]);
    }
} 
