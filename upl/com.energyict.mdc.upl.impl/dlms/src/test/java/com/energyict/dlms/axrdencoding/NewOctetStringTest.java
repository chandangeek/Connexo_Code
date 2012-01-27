package com.energyict.dlms.axrdencoding;

import junit.framework.TestCase;
import org.junit.Test;

import static com.energyict.protocolimpl.utils.ProtocolTools.getBytesFromHexString;
import static org.junit.Assert.assertArrayEquals;

/**
 * Copyrights EnergyICT
 * Date: 27/01/12
 * Time: 11:04
 */
public class NewOctetStringTest extends TestCase {

    @Test
    public void testBERConstructor() throws Exception {
        byte[] rawData = getBytesFromHexString("$09$04$01$02$03$04$FF$FF$FF$FF$FF$FF$FF$FF");
        NewOctetString os = new NewOctetString(rawData);
        assertNotNull(os.getBEREncodedByteArray());
        assertArrayEquals(getBytesFromHexString("$09$04$01$02$03$04"), os.getBEREncodedByteArray());
        assertEquals(6, os.size());
    }

    @Test
    public void testBERConstructorOffset() throws Exception {
        byte[] rawData = getBytesFromHexString("$FF$FF$FF$FF$FF$FF$09$04$01$02$03$04$FF$FF$FF$FF$FF$FF$FF$FF");
        NewOctetString os = new NewOctetString(rawData, 6);
        assertNotNull(os.getBEREncodedByteArray());
        assertArrayEquals(getBytesFromHexString("$09$04$01$02$03$04"), os.getBEREncodedByteArray());
        assertEquals(6, os.size());
    }
}
