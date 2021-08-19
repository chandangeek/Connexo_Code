package com.energyict.protocolimplv2.umi.packet.payload;

import com.energyict.protocolimplv2.umi.types.UmiCode;
import org.junit.Test;

import java.security.InvalidParameterException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ReadObjCmdPayloadTest {
    @Test
    public void createReadObjCmdPayload() {
        UmiCode code = new UmiCode("umi.0.1.0.1");
        ReadObjCmdPayload payload = new ReadObjCmdPayload(code);
        assertEquals(code, payload.getUmiCode());
        assertEquals(ReadObjCmdPayload.SIZE, payload.getLength());
    }

    @Test
    public void recreateReadObjCmdPayloadFromRaw() {
        UmiCode code = new UmiCode("umi.0.1.0.1");
        ReadObjCmdPayload payload = new ReadObjCmdPayload(code);
        ReadObjCmdPayload payload1 = new ReadObjCmdPayload(payload.getRaw());
        assertEquals(code, payload1.getUmiCode());
        assertEquals(ReadObjCmdPayload.SIZE, payload1.getLength());
        assertArrayEquals(payload.getRaw(), payload1.getRaw());
    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnInvalidRawData1() {
        new ReadObjCmdPayload(new byte[0]);
    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnInvalidRawData2() {
        new ReadObjCmdPayload(new byte[ReadObjCmdPayload.SIZE + 1]);
    }
} 
