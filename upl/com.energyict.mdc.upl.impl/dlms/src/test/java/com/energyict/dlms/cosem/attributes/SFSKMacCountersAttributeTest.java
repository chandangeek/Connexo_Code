package com.energyict.dlms.cosem.attributes;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Copyrights EnergyICT
 * Date: 31/01/12
 * Time: 16:07
 */
public class SFSKMacCountersAttributeTest {

    @Test
    public void testFindByAttributeNumber() throws Exception {
        for (SFSKMacCountersAttribute  attr : SFSKMacCountersAttribute.values()) {
            assertEquals(attr, SFSKMacCountersAttribute.findByAttributeNumber(attr.getAttributeNumber()));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindByAttributeNumberInvalid() throws Exception {
        SFSKMacCountersAttribute.findByAttributeNumber(-1);
    }

    @Test
    public void testFindByShortName() throws Exception {
        for (SFSKMacCountersAttribute attr : SFSKMacCountersAttribute.values()) {
            assertEquals(attr, SFSKMacCountersAttribute.findByShortName(attr.getShortName()));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindByShortNameInvalid() throws Exception {
        SFSKMacCountersAttribute.findByShortName(-1);
    }


}
