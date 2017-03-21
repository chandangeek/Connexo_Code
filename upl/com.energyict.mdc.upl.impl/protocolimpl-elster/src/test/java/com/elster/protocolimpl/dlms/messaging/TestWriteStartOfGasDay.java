package com.elster.protocolimpl.dlms.messaging;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by heuckeg on 24.04.2014.
 */
public class TestWriteStartOfGasDay {
    @Test
    public void testFullTime() throws IllegalArgumentException {
        Integer[] result = A1WriteStartOfGasDayMessage.processTimeString("23:59:59");
        assertNotNull(result);
        assertEquals(result.length, 4);
        System.out.println(result[0] + ":" + result[1] + ":" + result[2] + ":" + result[3]);
        assertEquals(23, (int) result[0]);
        assertEquals(59, (int) result[1]);
        assertEquals(59, (int) result[2]);
        assertEquals(255, (int) result[3]);
    }

    @Test
    public void testTimeWithoutSeconds() throws IllegalArgumentException {
        Integer[] result = A1WriteStartOfGasDayMessage.processTimeString("23:59");
        assertNotNull(result);
        assertEquals(result.length, 4);
        System.out.println(result[0] + ":" + result[1] + ":" + result[2] + ":" + result[3]);
        assertEquals(23, (int) result[0]);
        assertEquals(59, (int) result[1]);
        assertEquals(0, (int) result[2]);
        assertEquals(255, (int) result[3]);
    }

    @Test
    public void testTimeWithoutMinutes() throws IllegalArgumentException {
        Integer[] result = A1WriteStartOfGasDayMessage.processTimeString("23");
        assertNotNull(result);
        assertEquals(result.length, 4);
        System.out.println(result[0] + ":" + result[1] + ":" + result[2] + ":" + result[3]);
        assertEquals(23, (int) result[0]);
        assertEquals(0, (int) result[1]);
        assertEquals(0, (int) result[2]);
        assertEquals(255, (int) result[3]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTimeEmpty() throws IllegalArgumentException {
        A1SetBillingPeriodStartMessage.processDateString("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFullTimeRangeError() throws IllegalArgumentException {
        A1SetBillingPeriodStartMessage.processDateString("24:00:00");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTimeSecondsError1() throws IllegalArgumentException {
        A1SetBillingPeriodStartMessage.processDateString("23:59:60");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTimeSecondsError2() throws IllegalArgumentException {
        A1SetBillingPeriodStartMessage.processDateString("23:59:");
    }

    @Test(expected = IllegalArgumentException.class)

    public void testTimeMinutesError1() throws IllegalArgumentException {
        A1SetBillingPeriodStartMessage.processDateString("23:60");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTimeMinutesError2() throws IllegalArgumentException {
        A1SetBillingPeriodStartMessage.processDateString("23:");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTimeHourError1() throws IllegalArgumentException {
        A1SetBillingPeriodStartMessage.processDateString("24");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTimeHourError2() throws IllegalArgumentException {
        A1SetBillingPeriodStartMessage.processDateString(":");
    }
}
