package com.energyict.protocolimpl.iec1107.abba1700;

import com.energyict.protocolimpl.utils.ProtocolTools;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Copyrights EnergyICT
 * Date: 16-sep-2010
 * Time: 14:50:06
 */
public class CustDefRegConfigTest {

    private static final byte[] INVALID = ProtocolTools.getBytesFromHexString("$01$01$02$02$03");
    private static final byte[] NORMAL = ProtocolTools.getBytesFromHexString("$01$01$02$02$03$03");
    private static final byte[] NORMAL_NULFIRST = ProtocolTools.getBytesFromHexString("$00$01$00$02$00$03");
    private static final byte[] EXTENDED = ProtocolTools.getBytesFromHexString("$01$01$01$01$01$02$02$02$02$02$03$03$03$03$03");
    private static final byte[] EXTENDED_NULFIRST = ProtocolTools.getBytesFromHexString("$00$00$00$00$01$00$00$00$00$02$00$00$00$03$00");
    private static final byte[] OTHER_BETWEEN = ProtocolTools.getBytesFromHexString("$01$01$02$02$03$03$01$02$03");
    private static final byte[] OTHER_LONG = ProtocolTools.getBytesFromHexString("$00$00$00$00$01$00$00$00$00$02$00$00$00$03$00$01$02$03$04");

    @Test(expected = IOException.class)
    public void testCreationException() throws Exception {
        new CustDefRegConfig(INVALID);
    }

    @Test
    public void testCreation() throws Exception {
        try {
            new CustDefRegConfig(NORMAL);
            new CustDefRegConfig(NORMAL_NULFIRST);
            new CustDefRegConfig(EXTENDED);
            new CustDefRegConfig(EXTENDED_NULFIRST);
            new CustDefRegConfig(OTHER_BETWEEN);
            new CustDefRegConfig(OTHER_LONG);
        } catch (IOException e) {
            fail("Should not catch exception: " + e.getMessage());
        }
    }

    @Test
    public void testGetCustRegSource() throws Exception {
        assertNotNull(new CustDefRegConfig(NORMAL).getCustRegSource());
        assertNotNull(new CustDefRegConfig(NORMAL_NULFIRST).getCustRegSource());
        assertNotNull(new CustDefRegConfig(EXTENDED).getCustRegSource());
        assertNotNull(new CustDefRegConfig(EXTENDED_NULFIRST).getCustRegSource());
        assertNotNull(new CustDefRegConfig(OTHER_BETWEEN).getCustRegSource());
        assertNotNull(new CustDefRegConfig(OTHER_LONG).getCustRegSource());

        for (int i = 0; i < 3; i++) {
            assertNotNull(new CustDefRegConfig(NORMAL).getCustRegSource()[i]);
            assertNotNull(new CustDefRegConfig(NORMAL_NULFIRST).getCustRegSource()[i]);
            assertNotNull(new CustDefRegConfig(EXTENDED).getCustRegSource()[i]);
            assertNotNull(new CustDefRegConfig(EXTENDED_NULFIRST).getCustRegSource()[i]);
            assertNotNull(new CustDefRegConfig(OTHER_BETWEEN).getCustRegSource()[i]);
            assertNotNull(new CustDefRegConfig(OTHER_LONG).getCustRegSource()[i]);
        }

        assertEquals(3, new CustDefRegConfig(NORMAL).getCustRegSource().length);
        assertEquals(3, new CustDefRegConfig(NORMAL_NULFIRST).getCustRegSource().length);
        assertEquals(3, new CustDefRegConfig(EXTENDED).getCustRegSource().length);
        assertEquals(3, new CustDefRegConfig(EXTENDED_NULFIRST).getCustRegSource().length);
        assertEquals(3, new CustDefRegConfig(OTHER_BETWEEN).getCustRegSource().length);
        assertEquals(3, new CustDefRegConfig(OTHER_LONG).getCustRegSource().length);

        for (int i = 0; i < 3; i++) {
            assertEquals(2, new CustDefRegConfig(NORMAL).getCustRegSource()[i].length);
            assertEquals(2, new CustDefRegConfig(NORMAL_NULFIRST).getCustRegSource()[i].length);
            assertEquals(5, new CustDefRegConfig(EXTENDED).getCustRegSource()[i].length);
            assertEquals(5, new CustDefRegConfig(EXTENDED_NULFIRST).getCustRegSource()[i].length);
            assertEquals(2, new CustDefRegConfig(OTHER_BETWEEN).getCustRegSource()[i].length);
            assertEquals(5, new CustDefRegConfig(OTHER_LONG).getCustRegSource()[i].length);
        }

    }

    @Test
    public void testGetRegSource() throws Exception {
        for (int i = 0; i < 3; i++) {
            assertEquals(i + 1, new CustDefRegConfig(NORMAL).getRegSource(i));
            assertEquals(i + 1, new CustDefRegConfig(NORMAL_NULFIRST).getRegSource(i));
            assertEquals(i + 1, new CustDefRegConfig(EXTENDED).getRegSource(i));
            assertEquals(i + 1, new CustDefRegConfig(EXTENDED_NULFIRST).getRegSource(i));
            assertEquals(i + 1, new CustDefRegConfig(OTHER_BETWEEN).getRegSource(i));
            assertEquals(i + 1, new CustDefRegConfig(OTHER_LONG).getRegSource(i));
        }
    }

    @Test
    public void testIsExtended() throws Exception {
        assertFalse(new CustDefRegConfig(NORMAL).isExtended());
        assertFalse(new CustDefRegConfig(NORMAL_NULFIRST).isExtended());
        assertTrue(new CustDefRegConfig(EXTENDED).isExtended());
        assertTrue(new CustDefRegConfig(EXTENDED_NULFIRST).isExtended());
        assertFalse(new CustDefRegConfig(OTHER_BETWEEN).isExtended());
        assertTrue(new CustDefRegConfig(OTHER_LONG).isExtended());
    }

    @Test
    public void testToString() throws Exception {
        assertNotNull(new CustDefRegConfig(NORMAL).toString());
        assertNotNull(new CustDefRegConfig(NORMAL_NULFIRST).toString());
        assertNotNull(new CustDefRegConfig(EXTENDED).toString());
        assertNotNull(new CustDefRegConfig(EXTENDED_NULFIRST).toString());
        assertNotNull(new CustDefRegConfig(OTHER_BETWEEN).toString());
        assertNotNull(new CustDefRegConfig(OTHER_LONG).toString());
    }
}
