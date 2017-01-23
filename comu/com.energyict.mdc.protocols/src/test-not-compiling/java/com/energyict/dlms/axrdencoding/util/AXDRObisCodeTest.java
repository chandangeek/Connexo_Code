/*
 *
 */
package com.energyict.dlms.axrdencoding.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.energyict.dlms.axrdencoding.NullData;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.obis.ObisCode;

/**
 * @author jme
 *
 */
public class AXDRObisCodeTest {

	private static final ObisCode	OBISCODE	= ObisCode.fromString("1.1.1.8.0.255");
	private static final OctetString OCTET 		= OctetString.fromString(OBISCODE.toString());

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.util.AXDRObisCode#encode(ObisCode)}.
	 */
	@Test
	public final void testEncode() {
		assertNull(AXDRObisCode.encode(null));
		assertNotNull(AXDRObisCode.encode(OBISCODE));
		assertArrayEquals(OCTET.getContentBytes(), AXDRObisCode.encode(OBISCODE).getContentBytes());
		assertArrayEquals(OCTET.getContentBytes(), AXDRObisCode.encode(AXDRObisCode.decode(OCTET)).getContentBytes());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.util.AXDRObisCode#decode(com.energyict.dlms.axrdencoding.AbstractDataType)}.
	 */
	@Test
	public final void testDecode() {
		assertNull(AXDRObisCode.decode(null));
		assertNull(AXDRObisCode.decode(new NullData()));
		assertNotNull(AXDRObisCode.decode(OCTET));
		assertEquals(OBISCODE, AXDRObisCode.decode(OCTET));
		assertEquals(OBISCODE, AXDRObisCode.decode(AXDRObisCode.encode(OBISCODE)));
	}

}
