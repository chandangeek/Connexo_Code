package com.energyict.protocolimplv2.elster.garnet.common;

import com.energyict.protocolimpl.utils.ProtocolTools;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author sva
 * @since 8/09/2015 - 11:06
 */
public class SerialNumberCompacterTest {

    private static final String SERIAL_NUMBER = "75062896";

    @Test
    public void testPackSerialNumber() throws Exception {
        byte[] bytes = SerialNumberCompacter.packSerialNumber(SERIAL_NUMBER);

        assertArrayEquals(ProtocolTools.getBytesFromHexString("75062896FFFFFFFF", ""), bytes);
    }

    @Test
    public void testUnPackSerialNumber() throws Exception {
        byte[] bytes = ProtocolTools.getBytesFromHexString("75062896FFFFFFFF", "");

        String unPackedSerialNumber = SerialNumberCompacter.unPackSerialNumber(bytes);

        assertEquals(SERIAL_NUMBER, unPackedSerialNumber);
    }
}