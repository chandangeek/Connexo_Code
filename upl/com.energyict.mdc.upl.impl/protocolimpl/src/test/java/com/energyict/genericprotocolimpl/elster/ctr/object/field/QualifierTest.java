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
        assertEquals(0.001, qlf.getKmoltAbsoluteFactor());
        assertEquals(Qualifier.TARIF_PRICE_BAND3, qlf.getTarif());
        assertEquals(Qualifier.STANDARD_TIME, qlf.getValueTime());
    }
}
