package com.energyict.dlms.axrdencoding;

import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class Unsigned64Test {

    byte[] UNSIGNED64_WITH_MAX_VALUE = { (byte) 0x15, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
    byte[] UNSIGNED64_WITH_MIN_VALUE = { (byte) 0x15, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
    byte[] UNSIGNED64_WITH_MSB_SET_TO_ONE = { (byte) 0x15, (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
    byte[] UNSIGNED64_WITH_LSB_SET_TO_ONE = { (byte) 0x15, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01};
    byte[] UNSIGNED64_WITH_MSB_AND_LSB_SET_TO_ONE = { (byte) 0x15, (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01};

    @Test
    public void testConstructionWithUnsigned64MaxValueBytesExpectTheSameBytes() throws IOException {
        Unsigned64 unsigned64 = new Unsigned64(UNSIGNED64_WITH_MAX_VALUE, 0);
        assertArrayEquals(UNSIGNED64_WITH_MAX_VALUE, unsigned64.doGetBEREncodedByteArray());
    }

    @Test
    public void testConstructionWithUnsigned64MinValueBytesExpectTheSameBytes() throws IOException {
        Unsigned64 unsigned64 = new Unsigned64(UNSIGNED64_WITH_MIN_VALUE, 0);
        assertArrayEquals(UNSIGNED64_WITH_MIN_VALUE, unsigned64.doGetBEREncodedByteArray());
    }

    @Test
    public void testConstructionWithUnsigned64MsbSetToOneValueBytesExpectTheSameBytes() throws IOException {
        Unsigned64 unsigned64 = new Unsigned64(UNSIGNED64_WITH_MSB_SET_TO_ONE, 0);
        assertArrayEquals(UNSIGNED64_WITH_MSB_SET_TO_ONE, unsigned64.doGetBEREncodedByteArray());
    }

    @Test
    public void testConstructionWithUnsigned64LsbSetToOneValueBytesExpectTheSameBytes() throws IOException {
        Unsigned64 unsigned64 = new Unsigned64(UNSIGNED64_WITH_LSB_SET_TO_ONE, 0);
        assertArrayEquals(UNSIGNED64_WITH_LSB_SET_TO_ONE, unsigned64.doGetBEREncodedByteArray());
    }

    @Test
    public void testConstructionWithUnsigned64MsbAndLsbSetToOneValueBytesExpectTheSameBytes() throws IOException {
        Unsigned64 unsigned64 = new Unsigned64(UNSIGNED64_WITH_MSB_AND_LSB_SET_TO_ONE, 0);
        assertArrayEquals(UNSIGNED64_WITH_MSB_AND_LSB_SET_TO_ONE, unsigned64.doGetBEREncodedByteArray());
    }

    @Test
    public void testConstructionWithUnsignedMaxValueBytesExpectBigIntegerWithMaxValue() throws IOException {
        Unsigned64 unsigned64 = new Unsigned64(UNSIGNED64_WITH_MAX_VALUE, 0);
        assertEquals(new BigInteger("18446744073709551615"), unsigned64.getValue());
    }

    @Test
    public void testConstructionWithUnsignedMinValueBytesExpectBigIntegerWithMinValue() throws IOException {
        Unsigned64 unsigned64 = new Unsigned64(UNSIGNED64_WITH_MIN_VALUE, 0);
        assertEquals(BigInteger.ZERO, unsigned64.getValue());
    }

    @Test
    public void testConstructionWithUnsignedMsbSetToOneValueBytesExpectBigIntegerWithMsbSetToOneValue() throws IOException {
        Unsigned64 unsigned64 = new Unsigned64(UNSIGNED64_WITH_MSB_SET_TO_ONE, 0);
        assertEquals(new BigInteger("9223372036854775808"), unsigned64.getValue());
    }

    @Test
    public void testConstructionWithUnsignedLsbSetToOneValueBytesExpectBigIntegerWithLsbSetToOneValue() throws IOException {
        Unsigned64 unsigned64 = new Unsigned64(UNSIGNED64_WITH_LSB_SET_TO_ONE, 0);
        assertEquals(BigInteger.ONE, unsigned64.getValue());
    }

    @Test
    public void testConstructionWithUnsignedMsbAndLsbSetToOneValueBytesExpectBigIntegerWithMsbAndLsbSetToOneValue() throws IOException {
        Unsigned64 unsigned64 = new Unsigned64(UNSIGNED64_WITH_MSB_AND_LSB_SET_TO_ONE, 0);
        assertEquals(new BigInteger("9223372036854775809"), unsigned64.getValue());
    }

    @Test
    public void testConstructionWithMaxValueBigIntegerExpectMaxValueBytes() {
        Unsigned64 unsigned64 = new Unsigned64(new BigInteger("18446744073709551615"));
        assertArrayEquals(UNSIGNED64_WITH_MAX_VALUE, unsigned64.doGetBEREncodedByteArray());
    }

    @Test
    public void testConstructionWithMinValueBigIntegerExpectMinValueBytes() {
        Unsigned64 unsigned64 = new Unsigned64(BigInteger.ZERO);
        assertArrayEquals(UNSIGNED64_WITH_MIN_VALUE, unsigned64.getBEREncodedByteArray());
    }

    @Test
    public void testConstructionWithMsbSetToOneValueBigIntegerExpectMsbSetToOneValueBytes() {
        Unsigned64 unsigned64 = new Unsigned64(new BigInteger("9223372036854775808"));
        assertArrayEquals(UNSIGNED64_WITH_MSB_SET_TO_ONE, unsigned64.getBEREncodedByteArray());
    }

    @Test
    public void testConstructionWithLsbSetToOneValueBigIntegerExpectLsbSetToOneValueBytes() {
        Unsigned64 unsigned64 = new Unsigned64(BigInteger.ONE);
        assertArrayEquals(UNSIGNED64_WITH_LSB_SET_TO_ONE, unsigned64.getBEREncodedByteArray());
    }

    @Test
    public void testConstructionWithMsbAndLsbSetToOneValueBigIntegerExpectMsbAndLsbSetToOneValueBytes() {
        Unsigned64 unsigned64 = new Unsigned64(new BigInteger("9223372036854775809"));
        assertArrayEquals(UNSIGNED64_WITH_MSB_AND_LSB_SET_TO_ONE, unsigned64.getBEREncodedByteArray());
    }

    @Test
    public void testConstructionWithMaxValueBigIntegerExpectLongMinusOne() {
        Unsigned64 unsigned64 = new Unsigned64(new BigInteger("18446744073709551615"));
        assertEquals(-1, unsigned64.longValue());
    }

    @Test
    public void testToStringForMaxValue() throws IOException {
        Unsigned64 unsigned64 = new Unsigned64(UNSIGNED64_WITH_MAX_VALUE, 0);
        assertEquals("Unsigned64=18446744073709551615\n", unsigned64.toString());
    }

    @Test
    public void testToStringForMinValue() throws IOException {
        Unsigned64 unsigned64 = new Unsigned64(UNSIGNED64_WITH_MIN_VALUE, 0);
        assertEquals("Unsigned64=0\n", unsigned64.toString());
    }

    @Test
    public void testToStringForMsbSetToOneValue() throws IOException {
        Unsigned64 unsigned64 = new Unsigned64(UNSIGNED64_WITH_MSB_SET_TO_ONE, 0);
        assertEquals("Unsigned64=9223372036854775808\n", unsigned64.toString());
    }

    @Test
    public void testToStringForLsbSetToOneValue() throws IOException {
        Unsigned64 unsigned64 = new Unsigned64(UNSIGNED64_WITH_LSB_SET_TO_ONE, 0);
        assertEquals("Unsigned64=1\n", unsigned64.toString());
    }

    @Test
    public void testToStringForMsbAndLsbSetToOneValue() throws IOException {
        Unsigned64 unsigned64 = new Unsigned64(UNSIGNED64_WITH_MSB_AND_LSB_SET_TO_ONE, 0);
        assertEquals("Unsigned64=9223372036854775809\n", unsigned64.toString());
    }

    @Test
    public void testConstructionWithBigIntegerValueOfLongMinusOneExpectMaxValueBytes() {
        Unsigned64 unsigned64 = new Unsigned64(BigInteger.valueOf(-1L));
        assertArrayEquals(UNSIGNED64_WITH_MAX_VALUE, unsigned64.doGetBEREncodedByteArray());
    }

    @Test
    public void testConstructionWithBigIntegerMaxValueExpectBigDecimalMaxValue() {
        Unsigned64 unsigned64 = new Unsigned64(new BigInteger("18446744073709551615"));
        assertEquals(new BigDecimal("18446744073709551615"), unsigned64.toBigDecimal());
    }

}
