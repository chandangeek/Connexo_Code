package com.energyict.protocolimplv2.umi.ei4.profile;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class EI4UmiReadingStatusBitsTest {
    @Test
    public void testFromBit() throws Exception {
        assertEquals(EI4UmiReadingStatusBits.CLOCK_SYNC, EI4UmiReadingStatusBits.fromBitNumber(0));
        assertEquals(EI4UmiReadingStatusBits.CLOCK_SET, EI4UmiReadingStatusBits.fromBitNumber(1));
        assertEquals(EI4UmiReadingStatusBits.ESTIMATED_VALUE, EI4UmiReadingStatusBits.fromBitNumber(2));
        assertEquals(EI4UmiReadingStatusBits.INVALID_INTERVAL_DATA, EI4UmiReadingStatusBits.fromBitNumber(3));
        assertEquals(EI4UmiReadingStatusBits.INVALID_TARIFF_STRUCTURE, EI4UmiReadingStatusBits.fromBitNumber(4));
        assertEquals(EI4UmiReadingStatusBits.SOFTWARE_RESTART, EI4UmiReadingStatusBits.fromBitNumber(5));
        assertEquals(EI4UmiReadingStatusBits.INCORRECT_DATETIME_INTERVAL, EI4UmiReadingStatusBits.fromBitNumber(6));
    }

    @Test
    public void testGetBit() {
        assertEquals(0, EI4UmiReadingStatusBits.CLOCK_SYNC.getBitNumber());
        assertEquals(1, EI4UmiReadingStatusBits.CLOCK_SET.getBitNumber());
        assertEquals(2, EI4UmiReadingStatusBits.ESTIMATED_VALUE.getBitNumber());
        assertEquals(3, EI4UmiReadingStatusBits.INVALID_INTERVAL_DATA.getBitNumber());
        assertEquals(4, EI4UmiReadingStatusBits.INVALID_TARIFF_STRUCTURE.getBitNumber());
        assertEquals(5, EI4UmiReadingStatusBits.SOFTWARE_RESTART.getBitNumber());
        assertEquals(6, EI4UmiReadingStatusBits.INCORRECT_DATETIME_INTERVAL.getBitNumber());
    }

    @Test
    public void testValues() {
        assertArrayEquals(EI4UmiReadingStatusBits.values(), EI4UmiReadingStatusBits.readingStatusBits);
    }

    @Test(expected = NoSuchFieldException.class)
    public void throwsExceptionOnIncorrectBit() throws NoSuchFieldException {
        EI4UmiReadingStatusBits result = EI4UmiReadingStatusBits.fromBitNumber(7);
    }
} 
