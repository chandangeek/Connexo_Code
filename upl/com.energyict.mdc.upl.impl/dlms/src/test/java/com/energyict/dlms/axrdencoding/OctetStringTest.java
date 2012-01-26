/**
 *
 */
package com.energyict.dlms.axrdencoding;

import org.junit.Test;

import static com.energyict.protocolimpl.utils.ProtocolTools.getBytesFromHexString;
import static org.junit.Assert.*;

/**
 * @author jme
 */
public class OctetStringTest {

    /**
     * Test method for {@link com.energyict.dlms.axrdencoding.OctetString#fromString(java.lang.String)}.
     */
    @Test
    public final void testFromStringString() {
        final String testString = "TestString";
        OctetString os = OctetString.fromString(testString);
        assertNotNull(os);
        assertEquals(testString.length() + 2, os.getDecodedSize());
    }

    /**
     * Test method for {@link com.energyict.dlms.axrdencoding.OctetString#fromIPv4Address(String)}.
     */
    @Test
    public final void fromIpAddressStringTest() {
        final OctetString oc = OctetString.fromIPv4Address("192.168.18.255");
        assertNotNull(oc);
        assertArrayEquals(getBytesFromHexString("$09$04$C0$A8$12$FF"), oc.getBEREncodedByteArray());
        assertArrayEquals(getBytesFromHexString("$04$C0$A8$12$FF"), oc.getContentByteArray());
        assertArrayEquals(getBytesFromHexString("$C0$A8$12$FF"), oc.getOctetStr());
        assertArrayEquals(getBytesFromHexString("$C0$A8$12$FF"), oc.toByteArray());
        assertArrayEquals(getBytesFromHexString("$12$FF"), oc.getContentBytes());
        assertEquals(6, oc.getDecodedSize());
        assertEquals(6, oc.size());
    }

    /**
     * Test method for {@link com.energyict.dlms.axrdencoding.OctetString#fromByteArray(byte[], int)}.
     */
    @Test
    public final void testFromByteArray() {
        final byte[] testArray = "TestString".getBytes();
        OctetString os = OctetString.fromByteArray(testArray, testArray.length);
        assertNotNull(os);
        assertEquals(testArray.length + 2, os.getDecodedSize());
    }


}
