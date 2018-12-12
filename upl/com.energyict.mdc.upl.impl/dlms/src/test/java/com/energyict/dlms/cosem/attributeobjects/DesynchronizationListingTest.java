package com.energyict.dlms.cosem.attributeobjects;

import com.energyict.dlms.axrdencoding.*;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Copyrights EnergyICT
 * Date: 1/02/12
 * Time: 8:06
 */
public class DesynchronizationListingTest {

    @Test
    public void testGetPhysicalLayer() throws Exception {
        for (int physicalLayer = 0; physicalLayer < 10; physicalLayer++) {
            Structure struct = new Structure(
                    new Unsigned32(physicalLayer),
                    new Unsigned32(2),
                    new Unsigned32(3),
                    new Unsigned32(4),
                    new Unsigned32(5)
            );
            DesynchronizationListing desync = new DesynchronizationListing(struct.getBEREncodedByteArray(), 0, 0);
            assertEquals(physicalLayer, desync.getPhysicalLayer());
            assertEquals(2, desync.getTimeoutNotAddressed());
            assertEquals(3, desync.getTimeoutCrcNotOk());
            assertEquals(4, desync.getWriteRequest());
            assertEquals(5, desync.getwrongInitiator());
        }
    }

    @Test
    public void testGetTimeoutNotAddressed() throws Exception {
        for (int notAddressed = 0; notAddressed < 10; notAddressed++) {
            Structure struct = new Structure(
                    new Unsigned32(1),
                    new Unsigned32(notAddressed),
                    new Unsigned32(3),
                    new Unsigned32(4),
                    new Unsigned32(5)
            );
            DesynchronizationListing desync = new DesynchronizationListing(struct.getBEREncodedByteArray(), 0, 0);
            assertEquals(1, desync.getPhysicalLayer());
            assertEquals(notAddressed, desync.getTimeoutNotAddressed());
            assertEquals(3, desync.getTimeoutCrcNotOk());
            assertEquals(4, desync.getWriteRequest());
            assertEquals(5, desync.getwrongInitiator());
        }
    }

    @Test
    public void testGetTimeoutCrcNotOk() throws Exception {
        for (int crc = 0; crc < 10; crc++) {
            Structure struct = new Structure(
                    new Unsigned32(1),
                    new Unsigned32(2),
                    new Unsigned32(crc),
                    new Unsigned32(4),
                    new Unsigned32(5)
            );
            DesynchronizationListing desync = new DesynchronizationListing(struct.getBEREncodedByteArray(), 0, 0);
            assertEquals(1, desync.getPhysicalLayer());
            assertEquals(2, desync.getTimeoutNotAddressed());
            assertEquals(crc, desync.getTimeoutCrcNotOk());
            assertEquals(4, desync.getWriteRequest());
            assertEquals(5, desync.getwrongInitiator());
        }
    }

    @Test
    public void testGetWriteRequest() throws Exception {
        for (int write = 0; write < 10; write++) {
            Structure struct = new Structure(
                    new Unsigned32(1),
                    new Unsigned32(2),
                    new Unsigned32(3),
                    new Unsigned32(write),
                    new Unsigned32(5)
            );
            DesynchronizationListing desync = new DesynchronizationListing(struct.getBEREncodedByteArray(), 0, 0);
            assertEquals(1, desync.getPhysicalLayer());
            assertEquals(2, desync.getTimeoutNotAddressed());
            assertEquals(3, desync.getTimeoutCrcNotOk());
            assertEquals(write, desync.getWriteRequest());
            assertEquals(5, desync.getwrongInitiator());
        }
    }

    @Test
    public void testGetwrongInitiator() throws Exception {
        for (int wrongInit = 0; wrongInit < 10; wrongInit++) {
            Structure struct = new Structure(
                    new Unsigned32(1),
                    new Unsigned32(2),
                    new Unsigned32(3),
                    new Unsigned32(4),
                    new Unsigned32(wrongInit)
            );
            DesynchronizationListing desync = new DesynchronizationListing(struct.getBEREncodedByteArray(), 0, 0);
            assertEquals(1, desync.getPhysicalLayer());
            assertEquals(2, desync.getTimeoutNotAddressed());
            assertEquals(3, desync.getTimeoutCrcNotOk());
            assertEquals(4, desync.getWriteRequest());
            assertEquals(wrongInit, desync.getwrongInitiator());
        }
    }

    @Test
    public void testAllNull() throws Exception {
        Structure structure = new Structure(
                new NullData(),
                new NullData(),
                new NullData(),
                new NullData(),
                new NullData()
        );
        DesynchronizationListing desync = new DesynchronizationListing(structure.getBEREncodedByteArray(), 0, 0);
        assertEquals(0, desync.getPhysicalLayer());
        assertEquals(0, desync.getTimeoutNotAddressed());
        assertEquals(0, desync.getTimeoutCrcNotOk());
        assertEquals(0, desync.getWriteRequest());
        assertEquals(0, desync.getwrongInitiator());

    }

    @Test
    public void testToString() throws Exception {
        Structure structure = new Structure(
                new NullData(),
                new NullData(),
                new NullData(),
                new NullData(),
                new NullData()
        );
        DesynchronizationListing desync = new DesynchronizationListing(structure.getBEREncodedByteArray(), 0, 0);
        assertNotNull(desync.toString());

        structure = new Structure(
                new Unsigned32(1),
                new Unsigned32(2),
                new Unsigned32(3),
                new Unsigned32(4),
                new Unsigned32(5)
        );
        desync = new DesynchronizationListing(structure.getBEREncodedByteArray(), 0, 0);
        assertNotNull(desync.toString());

    }
}
