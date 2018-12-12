package com.energyict.dlms.axrdencoding;

import com.energyict.dlms.DLMSUtils;
import org.junit.Test;

import static junit.framework.Assert.*;

/**
 * Copyrights EnergyICT
 * Date: 8/11/11
 * Time: 11:26
 */
public class BitStringTest {

    @Test
    public void testLargeBitString() {
        byte[] rawValue = DLMSUtils.getBytesFromHexString("$04$82$02$00$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF$FF");
        try {
            BitString bitString = new BitString(rawValue, 0);
            assertNotNull(bitString);
            assertTrue(bitString.isBitString());
            assertEquals(rawValue.length, bitString.size());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

}
