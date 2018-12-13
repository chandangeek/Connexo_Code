package com.energyict.dlms.cosem.attributes;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Copyrights EnergyICT
 * Date: 31/01/12
 * Time: 16:07
 */
public class SFSKPhyMacSetupAttributeTest {

    @Test
    public void testFindByAttributeNumber() throws Exception {
        for (SFSKPhyMacSetupAttribute attr : SFSKPhyMacSetupAttribute.values()) {
            assertEquals(attr, SFSKPhyMacSetupAttribute.findByAttributeNumber(attr.getAttributeNumber()));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindByAttributeNumberInvalid() throws Exception {
        SFSKPhyMacSetupAttribute.findByAttributeNumber(-1);
    }

    @Test
    public void testFindByShortName() throws Exception {
        for (SFSKPhyMacSetupAttribute attr : SFSKPhyMacSetupAttribute.values()) {
            assertEquals(attr, SFSKPhyMacSetupAttribute.findByShortName(attr.getShortName()));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindByShortNameInvalid() throws Exception {
        SFSKPhyMacSetupAttribute.findByShortName(-1);
    }


}
