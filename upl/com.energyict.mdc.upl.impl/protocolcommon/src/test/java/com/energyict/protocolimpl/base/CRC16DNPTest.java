package com.energyict.protocolimpl.base;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Copyrights EnergyICT
 * Date: 11-aug-2010
 * Time: 11:51:48
 */
public class CRC16DNPTest {

    @Test
    public void testCalcCRC() throws Exception {
        assertEquals(0x5AD3, CRC16DNP.calcCRC("ABC".getBytes()));
        assertEquals(0x1BBC, CRC16DNP.calcCRC("1234567890".getBytes()));
        assertEquals(0x4F8D, CRC16DNP.calcCRC("TEST".getBytes()));
        assertEquals(0x5F74, CRC16DNP.calcCRC("12345678901234567890".getBytes()));
    }
}
