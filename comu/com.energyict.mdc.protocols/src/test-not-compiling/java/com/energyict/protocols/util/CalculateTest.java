package com.energyict.protocols.util;

import com.energyict.protocols.util.Calculate;
import org.junit.*;

import java.math.BigDecimal;

import static junit.framework.Assert.assertEquals;

/**
 * Copyrights EnergyICT
 * Date: 11-okt-2010
 * Time: 15:09:59
 */
public class CalculateTest {

    @Test
    public void testNormSignedFP2NumberLE() {
        byte[] data = {(byte) 0x03, (byte) 0x00};
        Number number = Calculate.convertNormSignedFP2NumberLE(data, 0);
        assertEquals(BigDecimal.valueOf(0.000091552734375), number);
        System.out.println(number);
    }

}