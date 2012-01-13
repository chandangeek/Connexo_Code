package com.energyict.smartmeterprotocolimpl.elster.apollo;

import org.junit.Test;

import static com.energyict.protocol.IntervalStateBits.*;
import static junit.framework.Assert.assertEquals;

/**
 * Copyrights EnergyICT
 * Date: 13/01/12
 * Time: 15:36
 */
public class AS300ProfileIntervalStatusBitsTest {

    @Test
    public void testGetEisStatusCode() throws Exception {
        AS300ProfileIntervalStatusBits bits = new AS300ProfileIntervalStatusBits();
        assertEquals(DEVICE_ERROR, bits.getEisStatusCode(0x01));
        assertEquals(BADTIME, bits.getEisStatusCode(0x02));
        assertEquals(CORRUPTED, bits.getEisStatusCode(0x04));
        assertEquals(OK, bits.getEisStatusCode(0x08));
        assertEquals(MISSING, bits.getEisStatusCode(0x10));
        assertEquals(SHORTLONG, bits.getEisStatusCode(0x20));
        assertEquals(OTHER, bits.getEisStatusCode(0x40));
        assertEquals(POWERDOWN, bits.getEisStatusCode(0x80));


        assertEquals(OK, bits.getEisStatusCode(0x00));

        assertEquals(
                DEVICE_ERROR | BADTIME,
                bits.getEisStatusCode(0x03)
        );

        assertEquals(
                DEVICE_ERROR | BADTIME | CORRUPTED,
                bits.getEisStatusCode(0x07)
        );

        assertEquals(
                DEVICE_ERROR | BADTIME | CORRUPTED,
                bits.getEisStatusCode(0x0F)
        );

        assertEquals(
                DEVICE_ERROR | BADTIME | CORRUPTED | MISSING,
                bits.getEisStatusCode(0x1F)
        );

        assertEquals(
                DEVICE_ERROR | BADTIME | CORRUPTED | MISSING | SHORTLONG,
                bits.getEisStatusCode(0x3F)
        );

        assertEquals(
                DEVICE_ERROR | BADTIME | CORRUPTED | MISSING | SHORTLONG | OTHER,
                bits.getEisStatusCode(0x7F)
        );

        assertEquals(
                DEVICE_ERROR | BADTIME | CORRUPTED | MISSING | SHORTLONG | OTHER | POWERDOWN,
                bits.getEisStatusCode(0xFF)
        );

    }
}
