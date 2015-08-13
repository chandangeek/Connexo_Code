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
public class TestA1V2ConfigureCyclicMode
{

    @Test
    public void validateText() throws BusinessException
    {
        TestClass tc = new TestClass();
        tc.doValidate("99 23:59:59");
        tc.doValidate("23:59:59");

        try
        {
            tc.doValidate("99 24:59:59");
            fail("99 24:59:59");
        }
        catch (BusinessException ignore)
        {
        }
        try
        {
            tc.doValidate("99 23:60:59");
            fail("99 23:60:59");
        }
        catch (BusinessException ignore)
        {
        }
        try
        {
            tc.doValidate("99 23:59:60");
            fail("99 23:59:60");
        }
        catch (BusinessException ignore)
        {
        }
    }

    @Test
    public void checkPatternGroups()
    {
        TestClass tc = new TestClass();
        String dist = "99 23:59:59";
        long distance =  tc.getComputedDistance(dist);
        assertEquals(dist, 0x63173B3B, distance);

        dist = "23:59:59";
        distance =  tc.getComputedDistance(dist);
        assertEquals(dist, 0x173B3B, distance);
    }

    private class TestClass extends A1ConfigureCyclicMode
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
            return computeDistance(distance);
        }
    }

}
