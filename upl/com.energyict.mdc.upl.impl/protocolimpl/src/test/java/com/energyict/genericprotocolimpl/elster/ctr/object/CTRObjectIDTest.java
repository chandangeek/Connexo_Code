package com.energyict.genericprotocolimpl.elster.ctr.object;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Copyrights EnergyICT
 * Date: 6-okt-2010
 * Time: 16:14:39
 */
public class CTRObjectIDTest {

    @Test
    public void testToString() throws Exception {
        assertEquals("1.2.3", new CTRObjectID(1, 2, 3).toString());
        assertEquals("3.1.2", new CTRObjectID(3, 1, 2).toString());
        assertEquals("A.B.C", new CTRObjectID(0x0A, 0x0B, 0x0C).toString());
        assertEquals("10.1.11", new CTRObjectID(0x10, 1, 0x11).toString());
    }

    @Test
    public void testGetX() throws Exception {
        assertEquals(1, new CTRObjectID(1, 2, 3).getX());
        assertEquals(3, new CTRObjectID(3, 1, 2).getX());
        assertEquals(10, new CTRObjectID(0x0A, 0x0B, 0x0C).getX());
        assertEquals(16, new CTRObjectID(0x10, 1, 0x11).getX());
    }

    @Test
    public void testGetY() throws Exception {
        assertEquals(2, new CTRObjectID(1, 2, 3).getY());
        assertEquals(1, new CTRObjectID(3, 1, 2).getY());
        assertEquals(11, new CTRObjectID(0x0A, 0x0B, 0x0C).getY());
        assertEquals(1, new CTRObjectID(0x10, 1, 0x11).getY());
    }

    @Test
    public void testGetZ() throws Exception {
        assertEquals(3, new CTRObjectID(1, 2, 3).getZ());
        assertEquals(2, new CTRObjectID(3, 1, 2).getZ());
        assertEquals(12, new CTRObjectID(0x0A, 0x0B, 0x0C).getZ());
        assertEquals(17, new CTRObjectID(0x10, 1, 0x11).getZ());
    }

    @Test
    public void testCreateFromString() throws Exception {
        assertEquals("1.2.3", new CTRObjectID("1.2.3").toString());
        assertEquals("3.1.2", new CTRObjectID("3.1.2").toString());
        assertEquals("A.B.C", new CTRObjectID("A.B.C").toString());
        assertEquals("10.1.11", new CTRObjectID("10.1.11").toString());
    }

}
