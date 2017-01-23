package com.energyict.dlms.axrdencoding.util;

import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.NullData;
import com.energyict.dlms.axrdencoding.OctetString;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author jme
 *
 */
public class AXDRUnitTest {

	private static final Unit	EXISTING_UNIT			= Unit.get("kW");
	private static final String	EXISTING_UNIT_STRING	= "27.3";
	private static final Unit	NO_UNIT					= Unit.get("");
	private static final String	NO_UNIT_STRING			= "255.0";

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.util.AXDRUnit#encode(Unit)}.
	 */
	@Test
	public final void testEncode() {
		assertNotNull(AXDRUnit.encode(EXISTING_UNIT));
		assertNotNull(AXDRUnit.encode(NO_UNIT));
		assertNull(AXDRUnit.encode(null));
		assertEquals(EXISTING_UNIT_STRING, AXDRUnit.encode(EXISTING_UNIT).stringValue());
		assertEquals(NO_UNIT_STRING, AXDRUnit.encode(NO_UNIT).stringValue());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.util.AXDRUnit#decode(com.energyict.dlms.axrdencoding.AbstractDataType)}.
	 */
	@Test
	public final void testDecode() {
		assertNull(AXDRUnit.decode(null));
		assertNull(AXDRUnit.decode(new NullData()));
		assertNull(AXDRUnit.decode(OctetString.fromByteArray("".getBytes())));
		assertNotNull(AXDRUnit.decode(OctetString.fromByteArray(EXISTING_UNIT_STRING.getBytes())));
		assertEquals(EXISTING_UNIT, AXDRUnit.decode(OctetString.fromByteArray(EXISTING_UNIT_STRING.getBytes())));
		assertEquals(NO_UNIT, AXDRUnit.decode(OctetString.fromByteArray(NO_UNIT_STRING.getBytes())));
	}

}
