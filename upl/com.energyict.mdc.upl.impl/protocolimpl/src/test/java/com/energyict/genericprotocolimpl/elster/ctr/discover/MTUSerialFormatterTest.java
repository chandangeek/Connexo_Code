package com.energyict.genericprotocolimpl.elster.ctr.discover;

import org.junit.Test;

import static com.energyict.genericprotocolimpl.elster.ctr.discover.MTUSerialFormatter.formatMTUSerialNumber;
import static org.junit.Assert.assertEquals;

/**
 * Copyrights EnergyICT
 * Date: 16/02/11
 * Time: 10:42
 */
public class MTUSerialFormatterTest {

    @Test
    public void testFormatMTUSerialNumber() throws Exception {
        assertEquals("ELS000000", formatMTUSerialNumber(null));
        assertEquals("ELS000000", formatMTUSerialNumber(""));
        assertEquals("ELS000000", formatMTUSerialNumber("testtesttesttest"));
        assertEquals("ELS000000", formatMTUSerialNumber("-004-"));
        assertEquals("ELS011234", formatMTUSerialNumber("2010-011234"));
        assertEquals("ELS000804", formatMTUSerialNumber("ELS2010-000804"));
        assertEquals("ELS321004", formatMTUSerialNumber("321004"));
        assertEquals("ELS000054", formatMTUSerialNumber("123000054"));
        assertEquals("ELS000014", formatMTUSerialNumber("ELS000014"));
        assertEquals("ELS000024", formatMTUSerialNumber("2010-024"));
        assertEquals("ELS000034", formatMTUSerialNumber("ELS2010-034"));
        assertEquals("ELS000044", formatMTUSerialNumber("044"));
        assertEquals("ELS000054", formatMTUSerialNumber("-054"));
        assertEquals("ELS000064", formatMTUSerialNumber("123-064"));
    }
}
