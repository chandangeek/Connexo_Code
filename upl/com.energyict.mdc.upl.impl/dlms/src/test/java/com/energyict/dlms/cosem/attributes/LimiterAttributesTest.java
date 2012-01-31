package com.energyict.dlms.cosem.attributes;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Copyrights EnergyICT
 * Date: 31/01/12
 * Time: 16:05
 */
public class LimiterAttributesTest {

    @Test
    public void testFindByAttributeNumber() throws Exception {
        for (LimiterAttributes attr : LimiterAttributes.values()) {
            assertEquals(attr, LimiterAttributes.findByAttributeNumber(attr.getAttributeNumber()));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindByAttributeNumberInvalid() throws Exception {
        LimiterAttributes.findByAttributeNumber(-1);
    }

}
