package com.elster.protocolimpl.dlms.messaging;

import com.energyict.cbo.BusinessException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * User: heuckeg
 * Date: 03.07.13
 * Time: 10:10
 */
public class TestA1V2ConfigurePreferredDateMode
{

    @Test
    public void validateText() throws BusinessException
    {
        TestClass tc = new TestClass();
        tc.doValidate("23:59:59");
        tc.doValidate("31 23:59:59");
        tc.doValidate("Mo 23:59:59");
        tc.doValidate("Tu 23:59:59");
        tc.doValidate("We 23:59:59");
        tc.doValidate("Th 23:59:59");
        tc.doValidate("Fr 23:59:59");
        tc.doValidate("Sa 23:59:59");
        tc.doValidate("Su 23:59:59");

        try
        {
            tc.doValidate("M 23:59:59");
            fail("M 32 23:59:59");
        }
        catch (BusinessException ignore)
        {
        }
        try
        {
            tc.doValidate("32 23:59:59");
            fail("32 23:59:59");
        }
        catch (BusinessException ignore)
        {
        }
        try
        {
            tc.doValidate("24:59:59");
            fail("24:59:59");
        }
        catch (BusinessException ignore)
        {
        }
        try
        {
            tc.doValidate("23:60:59");
            fail("23:60:59");
        }
        catch (BusinessException ignore)
        {
        }
        try
        {
            tc.doValidate("23:59:60");
            fail("23:59:60");
        }
        catch (BusinessException ignore)
        {
        }
    }

    @Test
    public void checkPatternGroups()
    {
        TestClass tc = new TestClass();
        String dist = "23:59:59";
        long distance =  tc.getComputedDistance(dist);
        assertEquals(dist, 0x00173B3BL, distance);

        dist = "15 23:59:59";
        distance =  tc.getComputedDistance(dist);
        assertEquals(dist, 0xF0173B3BL, distance);

        dist = "Mo 23:59:59";
        distance =  tc.getComputedDistance(dist);
        assertEquals(dist, 0x01173B3BL, distance);
    }

    private class TestClass extends A1ConfigurePreferredDateMode
    {
        public TestClass()
        {
            super(null);
        }

        public void doValidate(final String testString) throws BusinessException
        {
            validateMessageData(testString);
        }

        public long getComputedDistance(final String distance)
        {
            return computeDate(distance);
        }
    }

}
