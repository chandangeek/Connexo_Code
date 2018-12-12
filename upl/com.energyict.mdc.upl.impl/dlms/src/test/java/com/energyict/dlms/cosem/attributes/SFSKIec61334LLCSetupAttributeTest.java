package com.energyict.dlms.cosem.attributes;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Copyrights EnergyICT
 * Date: 31/01/12
 * Time: 16:07
 */
public class SFSKIec61334LLCSetupAttributeTest {

    @Test
    public void testFindByAttributeNumber() throws Exception {
        for (SFSKIec61334LLCSetupAttribute  attr : SFSKIec61334LLCSetupAttribute.values()) {
            assertEquals(attr, SFSKIec61334LLCSetupAttribute.findByAttributeNumber(attr.getAttributeNumber()));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindByAttributeNumberInvalid() throws Exception {
        SFSKIec61334LLCSetupAttribute.findByAttributeNumber(-1);
    }

    @Test
    public void testFindByShortName() throws Exception {
        for (SFSKIec61334LLCSetupAttribute attr : SFSKIec61334LLCSetupAttribute.values()) {
            assertEquals(attr, SFSKIec61334LLCSetupAttribute.findByShortName(attr.getShortName()));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindByShortNameInvalid() throws Exception {
        SFSKIec61334LLCSetupAttribute.findByShortName(-1);
    }


}
