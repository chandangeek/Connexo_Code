package com.energyict.protocolimplv2.umi.packet.payload;

import com.energyict.protocolimplv2.umi.types.UmiCode;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.security.InvalidParameterException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class WriteObjCmdPayloadTest {
    @Test
    public void createWriteObjCmdPayload() {
        UmiCode code = new UmiCode("umi.1.2.3.4");
        byte[] value = ByteBuffer.allocate(4).putInt(13).array();

        WriteObjCmdPayload payload = new WriteObjCmdPayload(code, value);
        assertEquals(code, payload.getUmiCode());
        assertArrayEquals(value, payload.getValue());
        assertEquals(value.length, payload.getValueLength());
    }

    @Test
    public void recreateWriteObjCmdPayloadFromRaw() {
        UmiCode code = new UmiCode("umi.1.2.3.4");
        byte[] value = ByteBuffer.allocate(4).putInt(13).array();

        WriteObjCmdPayload payload = new WriteObjCmdPayload(code, value);
        WriteObjCmdPayload payload1 = new WriteObjCmdPayload(payload.getRaw());
        assertEquals(payload.getUmiCode(), payload1.getUmiCode());
        assertArrayEquals(payload.getValue(), payload1.getValue());
        assertEquals(payload.getValueLength(), payload1.getValueLength());
    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnInvalidRawData() {
        new WriteObjCmdPayload(new byte[0]);
    }
} 
