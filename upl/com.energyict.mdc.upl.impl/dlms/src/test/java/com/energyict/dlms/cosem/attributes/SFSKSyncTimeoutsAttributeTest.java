package com.energyict.dlms.cosem.attributes;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Copyrights EnergyICT
 * Date: 31/01/12
 * Time: 16:07
 */
public class SFSKSyncTimeoutsAttributeTest {

    @Test
    public void testFindByAttributeNumber() throws Exception {
        for (SFSKSyncTimeoutsAttribute  attr : SFSKSyncTimeoutsAttribute.values()) {
            assertEquals(attr, SFSKSyncTimeoutsAttribute.findByAttributeNumber(attr.getAttributeNumber()));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindByAttributeNumberInvalid() throws Exception {
        SFSKSyncTimeoutsAttribute.findByAttributeNumber(-1);
    }

    @Test
    public void testFindByShortName() throws Exception {
        for (SFSKSyncTimeoutsAttribute attr : SFSKSyncTimeoutsAttribute.values()) {
            assertEquals(attr, SFSKSyncTimeoutsAttribute.findByShortName(attr.getShortName()));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindByShortNameInvalid() throws Exception {
        SFSKSyncTimeoutsAttribute.findByShortName(-1);
    }


}
