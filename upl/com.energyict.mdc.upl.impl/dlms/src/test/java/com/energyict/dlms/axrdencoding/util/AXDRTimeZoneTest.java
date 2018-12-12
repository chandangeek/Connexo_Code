/**
 *
 */
package com.energyict.dlms.axrdencoding.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.TimeZone;

import org.junit.Test;

import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.OctetString;

/**
 * @author jme
 *
 */
public class AXDRTimeZoneTest {

	private static final TimeZone		TIME_ZONE	= TimeZone.getTimeZone("GMT+5");
	private static final OctetString	OCTET		= OctetString.fromString(TIME_ZONE.getID());

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.util.AXDRTimeZone#encode(java.util.TimeZone)}.
	 */
	@Test
	public final void testEncode() {
		assertNull(AXDRTimeZone.encode(null));
		assertNotNull(AXDRTimeZone.encode(TIME_ZONE));
		assertEquals(OCTET.stringValue(), AXDRTimeZone.encode(TIME_ZONE).stringValue());
		assertEquals(OCTET.stringValue(), AXDRTimeZone.encode(AXDRTimeZone.decode(OCTET)).stringValue());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.util.AXDRTimeZone#decode(com.energyict.dlms.axrdencoding.AbstractDataType)}.
	 */
	@Test
	public final void testDecode() {
		assertNull(AXDRTimeZone.decode(null));
		assertNull(AXDRTimeZone.decode(new Integer8(0)));
		assertNotNull(AXDRTimeZone.decode(OCTET));
		assertEquals(TIME_ZONE, AXDRTimeZone.decode(OCTET));
		assertEquals(TIME_ZONE, AXDRTimeZone.decode(AXDRTimeZone.encode(TIME_ZONE)));
	}

}
