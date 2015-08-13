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
public class TestBillingPeriodStartMessage
{
    @Test
    public void testFullDate() throws BusinessException
    {
        Integer[] result = A1SetBillingPeriodStartMessage.processDateString("2014-04-24 Su");
        assertNotNull(result);
        assertEquals(result.length, 4);
        System.out.println(result[0] + "-" + result[1] + "-" + result[2] + "-" + result[3]);
        assertEquals(2014, (int)result[0]);
        assertEquals(4, (int)result[1]);
        assertEquals(24, (int)result[2]);
        assertEquals(7, (int)result[3]);
    }

    @Test
    public void testDateWithoutWeekday() throws BusinessException
    {
        Integer[] result = A1SetBillingPeriodStartMessage.processDateString("2014-04-24");
        assertNotNull(result);
        assertEquals(result.length, 4);
        System.out.println(result[0] + "-" + result[1] + "-" + result[2] + "-" + result[3]);
        assertEquals(2014, (int)result[0]);
        assertEquals(4, (int)result[1]);
        assertEquals(24, (int)result[2]);
        assertEquals(255, (int)result[3]);
    }

    @Test
    public void testDateWithoutYear() throws BusinessException
    {
        Integer[] result = A1SetBillingPeriodStartMessage.processDateString("-04-24");
        assertNotNull(result);
        assertEquals(result.length, 4);
        System.out.println(result[0] + "-" + result[1] + "-" + result[2] + "-" + result[3]);
        assertEquals(0xFFFF, (int)result[0]);
        assertEquals(4, (int)result[1]);
        assertEquals(24, (int)result[2]);
        assertEquals(255, (int)result[3]);
    }

    @Test
    public void testDateWithoutYearMonth() throws BusinessException
    {
        Integer[] result = A1SetBillingPeriodStartMessage.processDateString("--24");
        assertNotNull(result);
        assertEquals(result.length, 4);
        System.out.println(result[0] + "-" + result[1] + "-" + result[2] + "-" + result[3]);
        assertEquals(0xFFFF, (int)result[0]);
        assertEquals(0xFF, (int)result[1]);
        assertEquals(24, (int)result[2]);
        assertEquals(0xFF, (int)result[3]);
    }

    @Test
    public void testDateEmpty() throws BusinessException
    {
        Integer[] result = A1SetBillingPeriodStartMessage.processDateString("--");
        assertNotNull(result);
        assertEquals(result.length, 4);
        System.out.println(result[0] + "-" + result[1] + "-" + result[2] + "-" + result[3]);
        assertEquals(0xFFFF, (int)result[0]);
        assertEquals(0xFF, (int)result[1]);
        assertEquals(0xFF, (int)result[2]);
        assertEquals(0xFF, (int)result[3]);
    }

}
