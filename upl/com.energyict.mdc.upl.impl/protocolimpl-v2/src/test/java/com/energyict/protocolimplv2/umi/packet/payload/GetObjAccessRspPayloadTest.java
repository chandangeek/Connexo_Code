package com.energyict.protocolimplv2.umi.packet.payload;

import com.energyict.protocolimplv2.umi.types.UmiCode;
import org.junit.Test;

import java.security.InvalidParameterException;

import static org.junit.Assert.assertEquals;

public class GetObjAccessRspPayloadTest {
    @Test
    public void createGetObjAccessCmdPayload() {
        UmiCode code = new UmiCode("umi.0.0.0.1");
        int readAccess = 111;
        int writeAccess = 222;
        GetObjAccessRspPayload payload = new GetObjAccessRspPayload(code,readAccess, writeAccess);
        assertEquals(code, payload.getUmiCode());
        assertEquals(readAccess, payload.getReadAccess());
        assertEquals(writeAccess, payload.getWriteAccess());
        assertEquals(GetObjAccessRspPayload.SIZE, payload.getLength());
    }

    @Test
    public void recreateGetObjAccessCmdPayloadFromRaw() {
        UmiCode code = new UmiCode("umi.0.0.0.1");
        int readAccess = 123;
        int writeAccess = 222;
        GetObjAccessRspPayload payload = new GetObjAccessRspPayload(code,readAccess, writeAccess);
        GetObjAccessRspPayload payload1 = new GetObjAccessRspPayload(payload.getRaw());
        assertEquals(code, payload1.getUmiCode());
        assertEquals(readAccess, payload1.getReadAccess());
        assertEquals(writeAccess, payload1.getWriteAccess());
        assertEquals(GetObjAccessRspPayload.SIZE, payload1.getLength());
    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnInvalidRawData() {
        GetObjAccessRspPayload payload = new GetObjAccessRspPayload(new byte[0]);
    }
} 
