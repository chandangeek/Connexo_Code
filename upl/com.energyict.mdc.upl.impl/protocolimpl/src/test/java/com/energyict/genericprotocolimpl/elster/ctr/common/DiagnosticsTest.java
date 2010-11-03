package com.energyict.genericprotocolimpl.elster.ctr.common;

import com.energyict.genericprotocolimpl.elster.ctr.mapping.Diagnostics;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Copyrights EnergyICT
 * Date: 27-okt-2010
 * Time: 17:26:05
 */
public class DiagnosticsTest {

    @Test
    public void testGetDescriptionFromCode() throws Exception {
        for (int i = 0; i < 0x16; i++) {
            String message = Diagnostics.getDescriptionFromCode(1 << i);
            assertNotNull(message);
        }
        assertEquals("", Diagnostics.getDescriptionFromCode(0));
    }
}
