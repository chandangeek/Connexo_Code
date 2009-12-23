/**
 *
 */
package com.energyict.dlms.axrdencoding.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

import com.energyict.dlms.axrdencoding.NullData;
import com.energyict.dlms.axrdencoding.Unsigned32;

/**
 * @author jme
 *
 */
public class AXDRDateTest {

	private static final int	MILLIS_PER_SECOND	= 1000;
	private static final long	DATE_TO_CHECK		= System.currentTimeMillis() / MILLIS_PER_SECOND;

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.util.AXDRDate#encode(java.util.Date)}.
	 */
	@Test
	public final void testEncode() {
		assertNotNull(AXDRDate.encode(null));
		assertNotNull(AXDRDate.encode(new Date()));
		assertTrue(AXDRDate.encode(null).isNullData());
		assertTrue(AXDRDate.encode(null) instanceof NullData);
		assertTrue(AXDRDate.encode(new Date(DATE_TO_CHECK)) instanceof Unsigned32);
		assertEquals(new Unsigned32(DATE_TO_CHECK).getValue(), AXDRDate.encode(new Date(DATE_TO_CHECK * MILLIS_PER_SECOND)).getUnsigned32().getValue());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.util.AXDRDate#decode(com.energyict.dlms.axrdencoding.AbstractDataType)}.
	 */
	@Test
	public final void testDecode() {
		assertNull(AXDRDate.decode(null));
		assertNull(AXDRDate.decode(new NullData()));
		assertNotNull(AXDRDate.decode(new Unsigned32(DATE_TO_CHECK)));
		assertEquals(new Date(DATE_TO_CHECK * MILLIS_PER_SECOND).getTime(), AXDRDate.decode(new Unsigned32(DATE_TO_CHECK)).getTime());
	}

}
