/*
 *
 */
package com.energyict.dlms.axrdencoding.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.energyict.dlms.axrdencoding.NullData;
import com.energyict.dlms.axrdencoding.OctetString;

/**
 * @author jme
 *
 */
public class AXDRStringTest {

	private static final String			TEST_STRING		= "TestStringValue";
	private static final String			OTHER_STRING	= TEST_STRING + "123";
	private static final OctetString	OCTET_STRING	= OctetString.fromString(TEST_STRING);
	private static final String			EMPTY_STRING	= "";

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.util.AXDRString#encode(java.lang.String)}.
	 */
	@Test
	public final void testEncode() {
		assertNotNull(AXDRString.encode(null));
		assertEquals(NullData.class, AXDRString.encode(null).getClass());
		assertNotNull(AXDRString.encode(TEST_STRING));
		assertNotNull(AXDRString.encode(EMPTY_STRING));
		assertArrayEquals(OCTET_STRING.getContentBytes(), AXDRString.encode(TEST_STRING).getOctetString().getContentBytes());
		assertArrayEquals(OCTET_STRING.getContentBytes(), AXDRString.encode(AXDRString.decode(OCTET_STRING)).getOctetString().getContentBytes());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.util.AXDRString#decode(com.energyict.dlms.axrdencoding.AbstractDataType)}.
	 */
	@Test
	public final void testDecode() {
		assertNull(AXDRString.decode(null));
		assertNull(AXDRString.decode(new NullData()));
		assertNotNull(AXDRString.decode(OCTET_STRING));
		assertEquals(TEST_STRING, AXDRString.decode(OCTET_STRING));
		assertEquals(TEST_STRING, AXDRString.decode(AXDRString.encode(TEST_STRING)));
		assertNotSame(TEST_STRING, AXDRString.decode(AXDRString.encode(OTHER_STRING)));
	}

}
