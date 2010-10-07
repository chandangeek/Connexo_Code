package com.energyict.genericprotocolimpl.elster.ctr.object.field;

import junit.framework.TestCase;
import org.junit.Test;

/**
 * Copyrights EnergyICT
 * Date: 7-okt-2010
 * Time: 14:07:17
 */
public class QualifierTest extends TestCase {

    @Test
    public void testQlf() {
        Qualifier qlf = new Qualifier(0xFB);
        assertEquals(0.001, qlf.getKmoltFactor());
        assertEquals("Data recorded against price band 3", qlf.getTarif());
        assertEquals("Reserved", qlf.getValueDescription());
        assertEquals("Standard time", qlf.getValueTime());
    }
}
