package com.energyict.dlms.axrdencoding;

import com.energyict.dlms.DLMSUtils;
import org.junit.Test;

import static junit.framework.Assert.*;

/**
 * Copyrights EnergyICT
 * Date: 8/11/11
 * Time: 11:26
 */
public class VisibleStringTest {

    @Test
    public void testVisibleString() {
        byte[] rawValue = DLMSUtils.getBytesFromHexString("$0A$05$46$46$20$20$20");
        try {
            VisibleString visibleString = new VisibleString(rawValue, 0);
            assertNotNull(visibleString);
            assertTrue(visibleString.isVisibleString());
            assertEquals("FF   ", visibleString.getStr());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

}
