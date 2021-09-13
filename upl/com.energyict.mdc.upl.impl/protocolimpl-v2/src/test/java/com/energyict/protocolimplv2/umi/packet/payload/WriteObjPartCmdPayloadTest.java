package com.energyict.protocolimplv2.umi.packet.payload;

import com.energyict.protocolimplv2.umi.types.UmiObjectPart;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.security.InvalidParameterException;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class WriteObjPartCmdPayloadTest {

    @Test
    public void createWriteObjPartCmdPayload() {
        UmiObjectPart objectPart = new UmiObjectPart("umi.1.255.245.3[1]/8");
        byte[] value = ByteBuffer.allocate(4).putInt(13).array();

        WriteObjPartCmdPayload payload = new WriteObjPartCmdPayload(objectPart, value);
        assertEquals(objectPart, payload.getObjectPart());
        assertArrayEquals(value, payload.getValue());
        assertEquals(value.length, payload.getValueLength());
        assertEquals(payload.getValue().length, payload.getValueLength());
        assertTrue(ReadObjPartRspPayload.MIN_SIZE < payload.getLength());
    }

    @Test
    public void recreateWriteObjPartCmdPayloadFromRaw() {
        UmiObjectPart objectPart = new UmiObjectPart("umi.1.255.245.3[1]/8");
        byte[] value = ByteBuffer.allocate(4).putInt(13).array();

        WriteObjPartCmdPayload payload = new WriteObjPartCmdPayload(objectPart, value);
        WriteObjPartCmdPayload payload1 = new WriteObjPartCmdPayload(payload.getRaw());
        assertEquals(payload.getObjectPart(), payload1.getObjectPart());
        assertArrayEquals(payload.getValue(), payload1.getValue());
        assertEquals(payload.getReserved(), payload1.getReserved());
        assertEquals(payload.getValueLength(), payload1.getValueLength());
        assertEquals(payload1.getValue().length, payload1.getValueLength());
        assertTrue(ReadObjPartRspPayload.MIN_SIZE < payload1.getLength());
    }

    @Test(expected = InvalidParameterException.class)
    public void throwsExceptionOnInvalidRawData() {
        new WriteObjPartCmdPayload(new byte[0]);
    }
} 
