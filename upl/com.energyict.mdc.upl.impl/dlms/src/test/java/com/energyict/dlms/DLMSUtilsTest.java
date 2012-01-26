package com.energyict.dlms;

import org.junit.Test;

import static com.energyict.dlms.DLMSUtils.*;
import static com.energyict.protocolimpl.utils.ProtocolTools.getBytesFromHexString;
import static org.junit.Assert.assertArrayEquals;
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

    @Test
    public void testGetAXDRLengthEncoding() throws Exception {
        assertArrayEquals(getBytesFromHexString("$00"), getAXDRLengthEncoding(0));
        assertArrayEquals(getBytesFromHexString("$01"), getAXDRLengthEncoding(1));
        assertArrayEquals(getBytesFromHexString("$03"), getAXDRLengthEncoding(3));
        assertArrayEquals(getBytesFromHexString("$07"), getAXDRLengthEncoding(7));
        assertArrayEquals(getBytesFromHexString("$0F"), getAXDRLengthEncoding(15));
        assertArrayEquals(getBytesFromHexString("$1F"), getAXDRLengthEncoding(31));
        assertArrayEquals(getBytesFromHexString("$3F"), getAXDRLengthEncoding(63));
        assertArrayEquals(getBytesFromHexString("$7F"), getAXDRLengthEncoding(127));
        assertArrayEquals(getBytesFromHexString("$81$FF"), getAXDRLengthEncoding(255));
        assertArrayEquals(getBytesFromHexString("$82$01$FF"), getAXDRLengthEncoding(511));
        assertArrayEquals(getBytesFromHexString("$82$03$FF"), getAXDRLengthEncoding(1023));
        assertArrayEquals(getBytesFromHexString("$82$07$FF"), getAXDRLengthEncoding(2047));
        assertArrayEquals(getBytesFromHexString("$82$0F$FF"), getAXDRLengthEncoding(4095));
        assertArrayEquals(getBytesFromHexString("$82$1F$FF"), getAXDRLengthEncoding(8191));
        assertArrayEquals(getBytesFromHexString("$82$3F$FF"), getAXDRLengthEncoding(16383));
        assertArrayEquals(getBytesFromHexString("$82$7F$FF"), getAXDRLengthEncoding(32767));
        assertArrayEquals(getBytesFromHexString("$82$FF$FF"), getAXDRLengthEncoding(65535));
        assertArrayEquals(getBytesFromHexString("$83$01$FF$FF"), getAXDRLengthEncoding(131071));
        assertArrayEquals(getBytesFromHexString("$83$03$FF$FF"), getAXDRLengthEncoding(262143));
        assertArrayEquals(getBytesFromHexString("$83$07$FF$FF"), getAXDRLengthEncoding(524287));
        assertArrayEquals(getBytesFromHexString("$83$0F$FF$FF"), getAXDRLengthEncoding(1048575));
        assertArrayEquals(getBytesFromHexString("$83$1F$FF$FF"), getAXDRLengthEncoding(2097151));
        assertArrayEquals(getBytesFromHexString("$83$3F$FF$FF"), getAXDRLengthEncoding(4194303));
        assertArrayEquals(getBytesFromHexString("$83$7F$FF$FF"), getAXDRLengthEncoding(8388607));
        assertArrayEquals(getBytesFromHexString("$83$FF$FF$FF"), getAXDRLengthEncoding(16777215));
    }

    @Test
    public void testGetAXDRLength() throws Exception {
        assertEquals(0, getAXDRLength(getBytesFromHexString("$00"), 0));
        assertEquals(1, getAXDRLength(getBytesFromHexString("$01"), 0));
        assertEquals(3, getAXDRLength(getBytesFromHexString("$03"), 0));
        assertEquals(7, getAXDRLength(getBytesFromHexString("$07"), 0));
        assertEquals(15, getAXDRLength(getBytesFromHexString("$0F"), 0));
        assertEquals(31, getAXDRLength(getBytesFromHexString("$1F"), 0));
        assertEquals(63, getAXDRLength(getBytesFromHexString("$3F"), 0));
        assertEquals(127, getAXDRLength(getBytesFromHexString("$7F"), 0));
        assertEquals(255, getAXDRLength(getBytesFromHexString("$81$FF"), 0));
        assertEquals(511, getAXDRLength(getBytesFromHexString("$82$01$FF"), 0));
        assertEquals(1023, getAXDRLength(getBytesFromHexString("$82$03$FF"), 0));
        assertEquals(2047, getAXDRLength(getBytesFromHexString("$82$07$FF"), 0));
        assertEquals(4095, getAXDRLength(getBytesFromHexString("$82$0F$FF"), 0));
        assertEquals(8191, getAXDRLength(getBytesFromHexString("$82$1F$FF"), 0));
        assertEquals(16383, getAXDRLength(getBytesFromHexString("$82$3F$FF"), 0));
        assertEquals(32767, getAXDRLength(getBytesFromHexString("$82$7F$FF"), 0));
        assertEquals(65535, getAXDRLength(getBytesFromHexString("$82$FF$FF"), 0));
        assertEquals(131071, getAXDRLength(getBytesFromHexString("$83$01$FF$FF"), 0));
        assertEquals(262143, getAXDRLength(getBytesFromHexString("$83$03$FF$FF"), 0));
        assertEquals(524287, getAXDRLength(getBytesFromHexString("$83$07$FF$FF"), 0));
        assertEquals(1048575, getAXDRLength(getBytesFromHexString("$83$0F$FF$FF"), 0));
        assertEquals(2097151, getAXDRLength(getBytesFromHexString("$83$1F$FF$FF"), 0));
        assertEquals(4194303, getAXDRLength(getBytesFromHexString("$83$3F$FF$FF"), 0));
        assertEquals(8388607, getAXDRLength(getBytesFromHexString("$83$7F$FF$FF"), 0));
        assertEquals(16777215, getAXDRLength(getBytesFromHexString("$83$FF$FF$FF"), 0));
    }

    @Test
    public void testGetAXDRLengthOffset() throws Exception {
        assertEquals(1, getAXDRLengthOffset(getBytesFromHexString("$00"), 0));
        assertEquals(1, getAXDRLengthOffset(getBytesFromHexString("$01"), 0));
        assertEquals(1, getAXDRLengthOffset(getBytesFromHexString("$03"), 0));
        assertEquals(1, getAXDRLengthOffset(getBytesFromHexString("$07"), 0));
        assertEquals(1, getAXDRLengthOffset(getBytesFromHexString("$0F"), 0));
        assertEquals(1, getAXDRLengthOffset(getBytesFromHexString("$1F"), 0));
        assertEquals(1, getAXDRLengthOffset(getBytesFromHexString("$3F"), 0));
        assertEquals(1, getAXDRLengthOffset(getBytesFromHexString("$7F"), 0));
        assertEquals(2, getAXDRLengthOffset(getBytesFromHexString("$81$FF"), 0));
        assertEquals(3, getAXDRLengthOffset(getBytesFromHexString("$82$01$FF"), 0));
        assertEquals(3, getAXDRLengthOffset(getBytesFromHexString("$82$03$FF"), 0));
        assertEquals(3, getAXDRLengthOffset(getBytesFromHexString("$82$07$FF"), 0));
        assertEquals(3, getAXDRLengthOffset(getBytesFromHexString("$82$0F$FF"), 0));
        assertEquals(3, getAXDRLengthOffset(getBytesFromHexString("$82$1F$FF"), 0));
        assertEquals(3, getAXDRLengthOffset(getBytesFromHexString("$82$3F$FF"), 0));
        assertEquals(3, getAXDRLengthOffset(getBytesFromHexString("$82$7F$FF"), 0));
        assertEquals(3, getAXDRLengthOffset(getBytesFromHexString("$82$FF$FF"), 0));
        assertEquals(4, getAXDRLengthOffset(getBytesFromHexString("$83$01$FF$FF"), 0));
        assertEquals(4, getAXDRLengthOffset(getBytesFromHexString("$83$03$FF$FF"), 0));
        assertEquals(4, getAXDRLengthOffset(getBytesFromHexString("$83$07$FF$FF"), 0));
        assertEquals(4, getAXDRLengthOffset(getBytesFromHexString("$83$0F$FF$FF"), 0));
        assertEquals(4, getAXDRLengthOffset(getBytesFromHexString("$83$1F$FF$FF"), 0));
        assertEquals(4, getAXDRLengthOffset(getBytesFromHexString("$83$3F$FF$FF"), 0));
        assertEquals(4, getAXDRLengthOffset(getBytesFromHexString("$83$7F$FF$FF"), 0));
        assertEquals(4, getAXDRLengthOffset(getBytesFromHexString("$83$FF$FF$FF"), 0));
    }

}
