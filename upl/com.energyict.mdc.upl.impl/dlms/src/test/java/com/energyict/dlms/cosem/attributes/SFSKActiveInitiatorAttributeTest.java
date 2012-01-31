package com.energyict.dlms.cosem.attributes;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Copyrights EnergyICT
 * Date: 31/01/12
 * Time: 16:07
 */
public class SFSKActiveInitiatorAttributeTest {

    @Test
    public void testFindByAttributeNumber() throws Exception {
        for (SFSKActiveInitiatorAttribute  attr : SFSKActiveInitiatorAttribute.values()) {
            assertEquals(attr, SFSKActiveInitiatorAttribute.findByAttributeNumber(attr.getAttributeNumber()));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindByAttributeNumberInvalid() throws Exception {
        SFSKActiveInitiatorAttribute.findByAttributeNumber(-1);
    }

    @Test
    public void testFindByShortName() throws Exception {
        for (SFSKActiveInitiatorAttribute attr : SFSKActiveInitiatorAttribute.values()) {
            assertEquals(attr, SFSKActiveInitiatorAttribute.findByShortName(attr.getShortName()));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindByShortNameInvalid() throws Exception {
        SFSKActiveInitiatorAttribute.findByShortName(-1);
    }


}
