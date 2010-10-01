package com.energyict.genericprotocolimpl.elster.ctr.frame.field;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Copyrights EnergyICT
 * Date: 1-okt-2010
 * Time: 8:56:02
 */
public class FunctionCodeTest {

    @Test
    public void testGetBytes() throws Exception {
        byte[] result = {(byte) 0x87};
        FunctionCode functionCode = new FunctionCode();
        functionCode.setFunctionCode(0x087);
        assertArrayEquals(result, functionCode.getBytes());
    }

    @Test
    public void testParse() throws Exception {
        byte[] result = {(byte) 0x87};
        byte[] rawBytes1 = {0x01, 0x02, (byte) 0x87};
        byte[] rawBytes2 = {0x01, 0x02, (byte) 0x87, 0x03};
        byte[] rawBytes3 = {(byte) 0x87, 0x03};
        assertArrayEquals(result, new FunctionCode().parse(result, 0).getBytes());
        assertArrayEquals(result, new FunctionCode().parse(rawBytes1, 2).getBytes());
        assertArrayEquals(result, new FunctionCode().parse(rawBytes2, 2).getBytes());
        assertArrayEquals(result, new FunctionCode().parse(rawBytes3, 0).getBytes());
    }

    @Test
    public void testGetEncryptionStatus() throws Exception {
        FunctionCode functionCode = new FunctionCode();
        functionCode.setFunctionCode(0x3F);
        assertEquals(EncryptionStatus.NO_ENCRYPTION, functionCode.getEncryptionStatus());
        functionCode.setFunctionCode(0x7F);
        assertEquals(EncryptionStatus.KEYC_ENCRYPTION, functionCode.getEncryptionStatus());
        functionCode.setFunctionCode(0xBF);
        assertEquals(EncryptionStatus.KEYT_ENCRYPTION, functionCode.getEncryptionStatus());
        functionCode.setFunctionCode(0xFF);
        assertEquals(EncryptionStatus.KEYF_ENCRYPTION, functionCode.getEncryptionStatus());
    }

    @Test
    public void testSetEncryptionStatus() throws Exception {
        FunctionCode functionCode = new FunctionCode();
        for (int i = 0; i <= 0x3F; i++) {
            functionCode.setFunctionCode(i);
            for (EncryptionStatus status : EncryptionStatus.values()) {
                if (status != EncryptionStatus.INVALID_ENCRYPTION) {
                    functionCode.setEncryptionStatus(status);
                    assertEquals(status, functionCode.getEncryptionStatus());
                    assertEquals(i, functionCode.getFunctionCode() & 0x3F);
                }
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetEncryptionStatusUnknown() throws Exception {
        FunctionCode functionCode = new FunctionCode();
        functionCode.setEncryptionStatus(EncryptionStatus.INVALID_ENCRYPTION);
    }

    @Test
    public void testSetFunctionCode() throws Exception {
        FunctionCode functionCode = new FunctionCode();
        for (int i = 0; i < 255; i++) {
            functionCode.setFunctionCode(i);
            assertEquals(i, functionCode.getFunctionCode());
            assertTrue(EncryptionStatus.INVALID_ENCRYPTION != functionCode.getEncryptionStatus());
        }
    }

}
