package com.energyict.protocolimplv2.umi.packet.payload;

import com.energyict.protocolimplv2.umi.types.UmiObjectPart;
import org.junit.Test;

import java.security.InvalidParameterException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ReadObjPartCmdPayloadTest {
    @Test
    public void createReadObjPartCmdPayload() {
        UmiObjectPart objectPart = new UmiObjectPart("umi.1.255.245.3[1]/8");
        ReadObjPartCmdPayload payload = new ReadObjPartCmdPayload(objectPart);
        assertEquals(objectPart, payload.getObjectPart());
        assertEquals(UmiObjectPart.SIZE, payload.getLength());
    }

    @Test
    public void recreateReadObjPartCmdPayloadFromRaw() {
        UmiObjectPart objectPart = new UmiObjectPart("umi.1.255.245.3[1]/8");
        ReadObjPartCmdPayload payload = new ReadObjPartCmdPayload(objectPart);
        ReadObjPartCmdPayload payload1 = new ReadObjPartCmdPayload(payload.getRaw());
        assertEquals(payload.getObjectPart(), payload1.getObjectPart());
        assertEquals(UmiObjectPart.SIZE, payload1.getLength());
        assertArrayEquals(payload.getRaw(), payload1.getRaw());
    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnInvalidRawData() {
        new ReadObjPartCmdPayload(new byte[0]);
    }
} 
