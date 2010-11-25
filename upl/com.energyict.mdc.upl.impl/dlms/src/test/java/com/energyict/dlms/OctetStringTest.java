/**
 *
 */
package com.energyict.dlms;

import com.energyict.util.LogFactory;
import org.apache.commons.logging.Log;
import org.junit.Ignore;
import org.junit.Test;

import com.energyict.dlms.axrdencoding.OctetString;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author jme
 *
 */
public class OctetStringTest {

    static Log logger = LogFactory.getLog(OctetStringTest.class);

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.OctetString#doGetBEREncodedByteArray()}.
	 */
	@Test
	@Ignore("Not implemented yet")
	public final void testDoGetBEREncodedByteArray() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.OctetString#size()}.
	 */
	@Test
	@Ignore("Not implemented yet")
	public final void testSize() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.OctetString#intValue()}.
	 */
	@Test
	@Ignore("Not implemented yet")
	public final void testIntValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.OctetString#toBigDecimal()}.
	 */
	@Test
	@Ignore("Not implemented yet")
	public final void testToBigDecimal() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.OctetString#longValue()}.
	 */
	@Test
	@Ignore("Not implemented yet")
	public final void testLongValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.OctetString#OctetString(byte[], int)}.
	 */
	@Test
	@Ignore("Not implemented yet")
	public final void testOctetStringByteArrayInt() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.OctetString#OctetString(byte[], int, boolean)}.
	 */
	@Test
	@Ignore("Not implemented yet")
	public final void testOctetStringByteArrayIntBoolean() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.OctetString#toString()}.
	 */
	@Test
	@Ignore("Not implemented yet")
	public final void testToString() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.OctetString#OctetString(byte[])}.
	 */
	@Test
	@Ignore("Not implemented yet")
	public final void testOctetStringByteArray() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.OctetString#OctetString(byte[], boolean)}.
	 */
	@Test
	@Ignore("Not implemented yet")
	public final void testOctetStringByteArrayBoolean() {
		fail("Not yet implemented"); // TODO
	}

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
	 * Test method for {@link com.energyict.dlms.axrdencoding.OctetString#fromIpAddressString(String)}.
	 */
    @Test
    public final void fromIpAddressStringTest() {
        final String ipAddress = "0.0.10.0.100.255";
        final String expected = "00000A0064FF";
        assertArrayEquals(new OctetString(DLMSUtils.hexStringToByteArray(expected)).getBEREncodedByteArray(), OctetString.fromIpAddressString(ipAddress).getBEREncodedByteArray());
    }

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.OctetString#fromString(java.lang.String, int)}.
	 */
	@Test
	@Ignore("Not implemented yet")
	public final void testFromStringStringInt() {
		fail("Not yet implemented"); // TODO
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

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.OctetString#fromString(java.lang.String, int, boolean)}.
	 */
	@Test
	@Ignore("Not implemented yet")
	public final void testFromStringStringIntBoolean() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.OctetString#stringValue()}.
	 */
	@Test
	@Ignore("Not implemented yet")
	public final void testStringValue() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.OctetString#getContentBytes()}.
	 */
	@Test
	@Ignore("Not implemented yet")
	public final void testGetContentBytes() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.OctetString#getOctetStr()}.
	 */
	@Test
	@Ignore("Not implemented yet")
	public final void testGetOctetStr() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.OctetString#setOctetStr(byte[])}.
	 */
	@Test
	@Ignore("Not implemented yet")
	public final void testSetOctetStr() {
		fail("Not yet implemented"); // TODO
	}

}
