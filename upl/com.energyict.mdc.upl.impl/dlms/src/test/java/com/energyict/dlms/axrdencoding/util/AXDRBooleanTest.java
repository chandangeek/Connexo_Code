/**
 *
 */
package com.energyict.dlms.axrdencoding.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.energyict.dlms.axrdencoding.Integer16;
import com.energyict.dlms.axrdencoding.Integer32;
import com.energyict.dlms.axrdencoding.Integer8;

/**
 * @author jme
 *
 */
public class AXDRBooleanTest {

	private static final int	FALSE_VALUE	= 0;
	private static final int	TRUE_VALUE	= 1;

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.util.AXDRBoolean#encode(boolean)}.
	 */
	@Test
	public final void testEncode() {
		assertNotNull(AXDRBoolean.encode(true));
		assertNotNull(AXDRBoolean.encode(false));
		assertTrue(AXDRBoolean.encode(true) instanceof Integer8);
		assertTrue(AXDRBoolean.encode(false) instanceof Integer8);
		assertEquals(new Integer8(TRUE_VALUE).getValue(),AXDRBoolean.encode(true).getValue());
		assertEquals(new Integer8(FALSE_VALUE).getValue(),AXDRBoolean.encode(false).getValue());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.util.AXDRBoolean#decode(com.energyict.dlms.axrdencoding.AbstractDataType)}.
	 */
	@Test
	public final void testDecode() {
		assertTrue(AXDRBoolean.decode(new Integer8(TRUE_VALUE)));
		assertTrue(AXDRBoolean.decode(new Integer16(TRUE_VALUE)));
		assertTrue(AXDRBoolean.decode(new Integer32(TRUE_VALUE)));

		assertFalse(AXDRBoolean.decode(new Integer8(FALSE_VALUE)));
		assertFalse(AXDRBoolean.decode(new Integer16(FALSE_VALUE)));
		assertFalse(AXDRBoolean.decode(new Integer32(FALSE_VALUE)));

		assertFalse(AXDRBoolean.decode(null));
		assertFalse(AXDRBoolean.decode(new Integer8(2)));
		assertFalse(AXDRBoolean.decode(new Integer8(-1)));
	}

}
