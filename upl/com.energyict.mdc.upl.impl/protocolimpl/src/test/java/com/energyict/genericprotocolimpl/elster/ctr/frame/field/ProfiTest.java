package com.energyict.genericprotocolimpl.elster.ctr.frame.field;

import com.energyict.protocolimpl.utils.ProtocolTools;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Copyrights EnergyICT
 * Date: 07-okt-2010
 * Time: 14:05:10
 */
public class ProfiTest {

    @Test
    public void testGetBytes() throws Exception {
        Profi profi = new Profi();
        assertArrayEquals(new byte[] {0x00}, profi.getBytes());
        profi.setLongFrame(true);
        assertArrayEquals(new byte[] {(byte) 0x80}, profi.getBytes());
        for (int i = 0; i < 0x0FF; i++) {
            profi.setProfi(i);
            byte[] byteValue = {(byte) i};
            assertArrayEquals(byteValue, profi.getBytes());
        }
    }

    @Test
    public void testGetParse() throws Exception {
        Profi profi = new Profi();
        for (int i = 0; i < 0x0FF; i++) {
            byte[] byteValue = {(byte) i};
            profi.parse(ProtocolTools.concatByteArrays(new byte[i], byteValue), i);
            assertArrayEquals(byteValue, profi.getBytes());
        }
    }

    @Test
    public void testSetGetLongFrame() throws Exception {
        Profi profi = new Profi();
        assertFalse(profi.isLongFrame());
        profi.setLongFrame(true);
        assertTrue(profi.isLongFrame());
        profi.setLongFrame(false);
        assertFalse(profi.isLongFrame());
    }

    @Test
    public void testSetGetProfi() throws Exception {
        Profi profi = new Profi();
        for (int i = 0; i < 0x0FF; i++) {
            profi.setProfi(i);
            byte[] byteValue = {(byte) i};
            assertArrayEquals(byteValue, profi.getBytes());
        }
    }

}
