package com.energyict.protocolimpl.dlms.elgama;

import com.energyict.protocolimpl.utils.ProtocolTools;
import junit.framework.TestCase;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 29-dec-2010
 * Time: 14:40:13
 */
public class G3BTest extends TestCase {

    public void test() throws IOException {
        byte[] bytes = ProtocolTools.getBytesFromHexString("$80$FF$FF");
        byte[] bytes2 = ProtocolTools.getBytesFromHexString("$FF$FF$FF");
        int test = ProtocolTools.getUnsignedIntFromBytes(bytes);
        int test2 = ProtocolTools.getUnsignedIntFromBytes(bytes2);
        assertTrue(test > 0);
        assertTrue(test2 > 0);

        G3B g3b = new G3B();
        assertEquals(g3b.getHexStringFromTimeShift(-59), "C5");
        assertEquals(g3b.getHexStringFromTimeShift(1), "01");
        assertEquals(g3b.getHexStringFromTimeShift(59), "3B");
        assertEquals(g3b.getHexStringFromTimeShift(-1), "FF");







    }
}
