package com.energyict.genericprotocolimpl.elster.ctr.object.field;

import junit.framework.TestCase;
import org.junit.Test;

/**
 * Copyrights EnergyICT
 * Date: 7-okt-2010
 * Time: 15:49:50
 */
public class AccessDescriptorTest extends TestCase {

    @Test
    public void testAccessType() {
        AccessDescriptor ac = new AccessDescriptor(0xEF);
        assertEquals("ABC", ac.getWritePermissions());
        assertEquals("ABCD", ac.getReadPermissions());

        AccessDescriptor ac2 = new AccessDescriptor(0xE1);
        assertEquals("ABC", ac2.getWritePermissions());
        assertEquals("D", ac2.getReadPermissions());
    }
}
