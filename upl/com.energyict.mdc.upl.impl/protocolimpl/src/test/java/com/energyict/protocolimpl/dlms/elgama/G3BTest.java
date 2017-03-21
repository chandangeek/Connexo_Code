package com.energyict.protocolimpl.dlms.elgama;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimpl.utils.ProtocolTools;
import junit.framework.TestCase;
import org.mockito.Mock;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 29-dec-2010
 * Time: 14:40:13
 */
public class G3BTest extends TestCase {

    @Mock
    private NlsService nlsService;
    @Mock
    private PropertySpecService propertySpecService;

    public void test() throws IOException {
        byte[] bytes = ProtocolTools.getBytesFromHexString("$80$FF$FF");
        byte[] bytes2 = ProtocolTools.getBytesFromHexString("$FF$FF$FF");
        int test = ProtocolTools.getUnsignedIntFromBytes(bytes);
        int test2 = ProtocolTools.getUnsignedIntFromBytes(bytes2);
        assertTrue(test > 0);
        assertTrue(test2 > 0);

        G3B g3b = new G3B(propertySpecService, nlsService);
        assertEquals(g3b.getHexStringFromTimeShift(-59), "C5");
        assertEquals(g3b.getHexStringFromTimeShift(1), "01");
        assertEquals(g3b.getHexStringFromTimeShift(59), "3B");
        assertEquals(g3b.getHexStringFromTimeShift(-1), "FF");
    }
}