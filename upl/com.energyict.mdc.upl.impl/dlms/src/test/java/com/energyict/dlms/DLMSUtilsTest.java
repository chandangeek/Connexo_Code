package com.energyict.dlms;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 23/11/11
 * Time: 16:08
 */
public class DLMSUtilsTest {

    /**
     * * Test the paring of float32 format ***
     */
    @Test
    public void testParseValue2long_CASE_FLOAT32_A() throws Exception {
        byte[] buffer = new byte[]{0x17, 63, -128, 0, 0};
        long expected = 1;
        long actual = DLMSUtils.parseValue2long(buffer);
        assertEquals(expected, actual, 0);
    }

    @Test
    public void testParseValue2long_CASE_FLOAT32_B() throws Exception {
        byte[] buffer = new byte[]{0x17, 71, 114, 104, 0};
        long expected = 62056;
        long actual = DLMSUtils.parseValue2long(buffer);
        assertEquals(expected, actual, 0);
    }

    @Test
    public void testParseValue2long_CASE_FLOAT32_C() throws Exception {
        byte[] buffer = new byte[]{0x17, 64, 0, 0, 1};
        long expected = (long) 2.000000238418579;
        long actual = DLMSUtils.parseValue2long(buffer);
        assertEquals(expected, actual, 0);
    }

    /**
     * * Test the paring of float64 format ***
     */
    @Test
    public void testParseValue2long_CASE_FLOAT64_A() throws Exception {
        byte[] buffer = new byte[]{0x18, 63, -16, 0, 0, 0, 0, 0, 0};
        long expected = 1;
        long actual = DLMSUtils.parseValue2long(buffer);
        assertEquals(expected, actual, 0);
    }

    @Test
    public void testParseValue2long_CASE_FLOAT64_B() throws Exception {
        byte[] buffer = new byte[]{0x18, 64, -18, 77, 0, 0, 0, 0, 0};
        long expected = 62056;
        long actual = DLMSUtils.parseValue2long(buffer);
        assertEquals(expected, actual, 0);
    }
}
