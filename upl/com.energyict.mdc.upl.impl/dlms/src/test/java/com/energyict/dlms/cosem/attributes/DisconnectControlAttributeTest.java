package com.energyict.dlms.cosem.attributes;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Copyrights EnergyICT
 * Date: 31/01/12
 * Time: 16:07
 */
public class DisconnectControlAttributeTest {

    @Test
    public void testFindByAttributeNumber() throws Exception {
        for (DisconnectControlAttribute  attr : DisconnectControlAttribute.values()) {
            assertEquals(attr, DisconnectControlAttribute.findByAttributeNumber(attr.getAttributeNumber()));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindByAttributeNumberInvalid() throws Exception {
        DisconnectControlAttribute.findByAttributeNumber(-1);
    }

    @Test
    public void testFindByShortName() throws Exception {
        for (DisconnectControlAttribute attr : DisconnectControlAttribute.values()) {
            assertEquals(attr, DisconnectControlAttribute.findByShortName(attr.getShortName()));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindByShortNameInvalid() throws Exception {
        DisconnectControlAttribute.findByShortName(-1);
    }


}
