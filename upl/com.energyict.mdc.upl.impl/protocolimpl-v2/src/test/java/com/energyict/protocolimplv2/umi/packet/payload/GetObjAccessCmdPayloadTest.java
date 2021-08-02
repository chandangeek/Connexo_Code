package com.energyict.protocolimplv2.umi.packet.payload;

import com.energyict.protocolimplv2.umi.types.UmiCode;
import org.junit.Test;

import java.security.InvalidParameterException;

import static org.junit.Assert.assertEquals;

public class GetObjAccessCmdPayloadTest {
    @Test
    public void createGetObjAccessCmdPayload() {
        UmiCode code = new UmiCode("umi.0.0.0.1");
        GetObjAccessCmdPayload payload = new GetObjAccessCmdPayload(code);
        assertEquals(code, payload.getUmiCode());
        assertEquals(GetObjAccessCmdPayload.SIZE, payload.getLength());
    }

    @Test
    public void recreateGetObjAccessCmdPayloadFromRaw() {
        UmiCode code = new UmiCode("umi.0.0.0.1");
        GetObjAccessCmdPayload payload = new GetObjAccessCmdPayload(code);
        GetObjAccessCmdPayload payload1 = new GetObjAccessCmdPayload(payload.getRaw());
        assertEquals(code, payload1.getUmiCode());
        assertEquals(GetObjAccessCmdPayload.SIZE, payload1.getLength());
    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnInvalidRawData() {
        GetObjAccessCmdPayload payload = new GetObjAccessCmdPayload(new byte[0]);
    }
} 
