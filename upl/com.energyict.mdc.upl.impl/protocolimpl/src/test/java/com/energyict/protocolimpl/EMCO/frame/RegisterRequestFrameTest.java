package com.energyict.protocolimpl.EMCO.frame;

import junit.framework.TestCase;

import static org.junit.Assert.assertArrayEquals;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 24/02/12
 * Time: 8:47
 */
public class RegisterRequestFrameTest extends TestCase {

    public void testGetBytes() throws Exception {
        RegisterRequestFrame frame = new RegisterRequestFrame(12, 49);
        byte[] bytes = frame.getBytes();

        byte[] expectedBytes = {    // ASCII CHARS
                0x3a,               // Start of request ':'
                0x31, 0x32,         // Device Unit number '12'
                0x23,               // Register request char '#'
                0x34, 0x39,         // Register unit number '49'
                0x0D                // End of request <Carriage return>
        };

        assertArrayEquals(expectedBytes, bytes);
    }
}