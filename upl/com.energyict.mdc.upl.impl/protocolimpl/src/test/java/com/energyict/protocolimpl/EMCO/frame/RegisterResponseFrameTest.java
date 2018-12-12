package com.energyict.protocolimpl.EMCO.frame;

import com.energyict.protocolimpl.base.ProtocolConnectionException;
import junit.framework.TestCase;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 24/02/12
 * Time: 9:09
 */
public class RegisterResponseFrameTest extends TestCase {

    public void testParseBytes_CorrectCRC() throws Exception {
        RegisterResponseFrame responseFrame = new RegisterResponseFrame();

        byte[] response = {                     // ASCII CHARS
                0x3a,                           // Start of response ':'
                0x30, 0x30, 0x30, 0x31, 0x32,   // Device Unit number '00012' padded with extra 0's
                0x23,                           // Register request char '#'
                0x34, 0x39,                     // Register unit number '49' padded with extra 0
                0x61,                           // Data type 'a' - floating point
                0x31, 0x32, 0x2e, 0x33,         // data '12.3'
                0x21,                           // '!' - faults are present
                0x30, 0x33,                           // ASCII hexadecimal modulo-256 checksum
                0x0D, 0x0A                      // End of response <Carriage Return> <Line Feed>
        };

        responseFrame.parseBytes(response);

        assertEquals(ResponseFrame.REGISTER_RESPONSE, responseFrame.getResponseType());
        assertEquals(12, responseFrame.getUnitNumber());
        assertEquals(49, responseFrame.getRegisterNumber());
        assertEquals('a', responseFrame.getDataType());
        assertEquals(new BigDecimal("12.3"), responseFrame.getValue());
        assertEquals(true, responseFrame.isFaultsPresent());
        assertEquals(response, responseFrame.getBytes());

        assertEquals(0, responseFrame.getBitMask());
        assertEquals(null, responseFrame.getText());
    }

    public void testParseBytes_CRC_Error() throws Exception {
        RegisterResponseFrame responseFrame = new RegisterResponseFrame();

        byte[] response = {                     // ASCII CHARS
                0x3a,                           // Start of response ':'
                0x30, 0x30, 0x30, 0x31, 0x32,   // Device Unit number '00012' padded with extra 0's
                0x23,                           // Register request char '#'
                0x34, 0x39,                     // Register unit number '49' padded with extra 0
                0x61,                           // Data type 'a' - floating point
                0x31, 0x32, 0x2e, 0x33,         // data '12.3'
                0x21,                           // '!' - faults are present
                0x30, 0x34,                           // ASCII hexadecimal modulo-256 checksum
                0x0D, 0x0A                      // End of response <Carriage Return> <Line Feed>
        };

        try {
            responseFrame.parseBytes(response);
            fail();     // Method didn't threw the expected exception.
        } catch (Throwable e) {
            assertEquals(ProtocolConnectionException.class, e.getClass());
            return;
        }
    }

    public void testCheckMatchingRequest_Matching() throws Exception {
        RegisterRequestFrame requestFrame = new RegisterRequestFrame(12, 49);
        RegisterResponseFrame responseFrame = new RegisterResponseFrame();

        byte[] response = {                     // ASCII CHARS
                0x3a,                           // Start of response ':'
                0x30, 0x30, 0x30, 0x31, 0x32,   // Device Unit number '00012' padded with extra 0's
                0x23,                           // Register request char '#'
                0x34, 0x39,                     // Register unit number '49' padded with extra 0
                0x61,                           // Data type 'a' - floating point
                0x31, 0x32, 0x2e, 0x33,         // data '12.3'
                0x21,                           // '!' - faults are present
                0x30, 0x33,                           // ASCII hexadecimal modulo-256 checksum
                0x0D, 0x0A                      // End of response <Carriage Return> <Line Feed>
        };

        responseFrame.parseBytes(response);

        // If the response doesn't match the request, a ProtocolConnectionException should be thrown - here no exceptions should be thrown.
        responseFrame.checkMatchingRequest(requestFrame);
    }

    @Test(expected= ProtocolConnectionException.class)
    public void testCheckMatchingRequest_NotMatching() throws Exception {


        RegisterRequestFrame requestFrame = new RegisterRequestFrame(112, 49);
        RegisterResponseFrame responseFrame = new RegisterResponseFrame();

        byte[] response = {                     // ASCII CHARS
                0x3a,                           // Start of response ':'
                0x30, 0x30, 0x30, 0x31, 0x32,   // Device Unit number '00012' padded with extra 0's
                0x23,                           // Register request char '#'
                0x34, 0x39,                     // Register unit number '49' padded with extra 0
                0x61,                           // Data type 'a' - floating point
                0x31, 0x32, 0x2e, 0x33,         // data '12.3'
                0x21,                           // '!' - faults are present
                0x30, 0x33,                           // ASCII hexadecimal modulo-256 checksum
                0x0D, 0x0A                      // End of response <Carriage Return> <Line Feed>
        };

        responseFrame.parseBytes(response);

        // If the response doesn't match the request, a ProtocolConnectionException should be thrown - here an exceptions should be thrown.
        try {
            responseFrame.checkMatchingRequest(requestFrame);
            fail();     // Method didn't threw the expected exception.
        } catch (Throwable e) {
            assertEquals(ProtocolConnectionException.class, e.getClass());
            return;
        }
    }
}