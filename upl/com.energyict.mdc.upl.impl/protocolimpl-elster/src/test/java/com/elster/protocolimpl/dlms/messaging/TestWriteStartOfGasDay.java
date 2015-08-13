package com.elster.protocolimpl.dlms.messaging;

import com.energyict.cbo.BusinessException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 * Created by heuckeg on 24.04.2014.
 *
 */
public class TestWriteStartOfGasDay
{
    @Test
    public void testFullTime() throws BusinessException
    {
        Integer[] result = A1WriteStartOfGasDayMessage.processTimeString("23:59:59");
        assertNotNull(result);
        assertEquals(result.length, 4);
        System.out.println(result[0] + ":" + result[1] + ":" + result[2] + ":" + result[3]);
        assertEquals(23, (int)result[0]);
        assertEquals(59, (int)result[1]);
        assertEquals(59, (int)result[2]);
        assertEquals(255, (int)result[3]);
    }

    @Test
    public void testTimeWithoutSeconds() throws BusinessException
    {
        Integer[] result = A1WriteStartOfGasDayMessage.processTimeString("23:59");
        assertNotNull(result);
        assertEquals(result.length, 4);
        System.out.println(result[0] + ":" + result[1] + ":" + result[2] + ":" + result[3]);
        assertEquals(23, (int)result[0]);
        assertEquals(59, (int)result[1]);
        assertEquals(0, (int)result[2]);
        assertEquals(255, (int)result[3]);
    }

    @Test
    public void testTimeWithoutMinutes() throws BusinessException
    {
        Integer[] result = A1WriteStartOfGasDayMessage.processTimeString("23");
        assertNotNull(result);
        assertEquals(result.length, 4);
        System.out.println(result[0] + ":" + result[1] + ":" + result[2] + ":" + result[3]);
        assertEquals(23, (int)result[0]);
        assertEquals(0, (int)result[1]);
        assertEquals(0, (int)result[2]);
        assertEquals(255, (int)result[3]);
    }

    @Test(expected = BusinessException.class)
    public void testTimeEmpty() throws BusinessException
    {
        A1SetBillingPeriodStartMessage.processDateString("");
    }

    @Test(expected = BusinessException.class)
    public void testFullTimeRangeError() throws BusinessException
    {
        A1SetBillingPeriodStartMessage.processDateString("24:00:00");
    }

    @Test(expected = BusinessException.class)
    public void testTimeSecondsError1() throws BusinessException
    {
        A1SetBillingPeriodStartMessage.processDateString("23:59:60");
    }

    @Test(expected = BusinessException.class)
    public void testTimeSecondsError2() throws BusinessException
    {
        A1SetBillingPeriodStartMessage.processDateString("23:59:");
    }
    @Test(expected = BusinessException.class)

    public void testTimeMinutesError1() throws BusinessException
    {
        A1SetBillingPeriodStartMessage.processDateString("23:60");
    }

    @Test(expected = BusinessException.class)
    public void testTimeMinutesError2() throws BusinessException
    {
        A1SetBillingPeriodStartMessage.processDateString("23:");
    }

    @Test(expected = BusinessException.class)
    public void testTimeHourError1() throws BusinessException
    {
        A1SetBillingPeriodStartMessage.processDateString("24");
    }

    @Test(expected = BusinessException.class)
    public void testTimeHourError2() throws BusinessException
    {
        A1SetBillingPeriodStartMessage.processDateString(":");
    }
}
