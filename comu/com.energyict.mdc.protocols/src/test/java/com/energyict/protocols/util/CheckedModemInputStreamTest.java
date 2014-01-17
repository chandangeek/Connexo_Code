package com.energyict.protocols.util;

import org.junit.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Copyrights EnergyICT
 * Date: 10/01/12
 * Time: 10:51
 */
public class CheckedModemInputStreamTest {

    public static final String ENCODING = "US-ASCII";

    @Test(expected = IOException.class)
    public void testReadSingleByte() throws Exception {
        byte[] ascii = "This is a test to see if the input stream detects the 'NO DIALTONE' word.".getBytes(ENCODING);
        CheckedModemInputStream checkedModemStream = new CheckedModemInputStream(new ByteArrayInputStream(ascii));
        while (checkedModemStream.read() != -1) {
            ;
        }
    }

    @Test(expected = IOException.class)
    public void testReadBuffer() throws Exception {
        byte[] ascii = "This is a test to see if the input stream detects the 'NO DIALTONE' word.".getBytes(ENCODING);
        CheckedModemInputStream checkedModemStream = new CheckedModemInputStream(new ByteArrayInputStream(ascii));
        while (checkedModemStream.read(new byte[5]) != -1) {
            ;
        }
    }

    @Test(expected = IOException.class)
    public void testReadBufferWithOffsetAndLength() throws Exception {
        byte[] ascii = "This is a test to see if the input stream detects the 'NO DIALTONE' word.".getBytes(ENCODING);
        CheckedModemInputStream checkedModemStream = new CheckedModemInputStream(new ByteArrayInputStream(ascii));
        while (checkedModemStream.read(new byte[6], 1, 5) != -1) {
            ;
        }
    }

    @Test
    public void testLowerCase() throws Exception {
        byte[] ascii = "This is a test to see if the input stream detects the 'no dialtone' word.".getBytes(ENCODING);
        CheckedModemInputStream checkedModemStream = new CheckedModemInputStream(new ByteArrayInputStream(ascii));
        while (checkedModemStream.read(new byte[6], 1, 5) != -1) {
            ;
        }
    }

    @Test(expected = IOException.class)
    public void testError() throws Exception {
        byte[] ascii = "This is a test to see if the input stream detects the 'ERROR' word.".getBytes(ENCODING);
        CheckedModemInputStream checkedModemStream = new CheckedModemInputStream(new ByteArrayInputStream(ascii));
        while (checkedModemStream.read(new byte[6], 1, 5) != -1) {
            ;
        }
    }

    @Test(expected = IOException.class)
    public void testNoDialtone() throws Exception {
        byte[] ascii = "This is a test to see if the input stream detects the 'NO DIALTONE' word.".getBytes(ENCODING);
        CheckedModemInputStream checkedModemStream = new CheckedModemInputStream(new ByteArrayInputStream(ascii));
        while (checkedModemStream.read(new byte[6], 1, 5) != -1) {
            ;
        }
    }

    @Test(expected = IOException.class)
    public void testBusy() throws Exception {
        byte[] ascii = "This is a test to see if the input stream detects the 'BUSY' word.".getBytes(ENCODING);
        CheckedModemInputStream checkedModemStream = new CheckedModemInputStream(new ByteArrayInputStream(ascii));
        while (checkedModemStream.read(new byte[6], 1, 5) != -1) {
            ;
        }
    }

    @Test(expected = IOException.class)
    public void testNoCarrier() throws Exception {
        byte[] ascii = "This is a test to see if the input stream detects the 'NO CARRIER' word.".getBytes(ENCODING);
        CheckedModemInputStream checkedModemStream = new CheckedModemInputStream(new ByteArrayInputStream(ascii));
        while (checkedModemStream.read(new byte[6], 1, 5) != -1) {
            ;
        }
    }

    @Test(expected = IOException.class)
    public void testNoAnswer() throws Exception {
        byte[] ascii = "This is a test to see if the input stream detects the 'NO ANSWER' word.".getBytes(ENCODING);
        CheckedModemInputStream checkedModemStream = new CheckedModemInputStream(new ByteArrayInputStream(ascii));
        while (checkedModemStream.read(new byte[6], 1, 5) != -1) {
            ;
        }
    }

    @Test
    public void testAvailable() throws Exception {
        CheckedModemInputStream checkedModemStream = new CheckedModemInputStream(new ByteArrayInputStream(new byte[64]));
        assertEquals(64, checkedModemStream.available());
        assertEquals(30, checkedModemStream.read(new byte[30]));
        assertEquals(34, checkedModemStream.available());
        assertEquals(34, checkedModemStream.read(new byte[64]));
        assertEquals(0, checkedModemStream.available());
    }

}
