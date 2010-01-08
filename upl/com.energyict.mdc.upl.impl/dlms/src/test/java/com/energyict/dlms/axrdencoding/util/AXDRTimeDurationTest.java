/**
 *
 */
package com.energyict.dlms.axrdencoding.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.energyict.cbo.TimeDuration;
import com.energyict.dlms.axrdencoding.Integer32;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.Structure;

/**
 * @author jme
 *
 */
public class AXDRTimeDurationTest {

	private static final TimeDuration	TIMEDURATION	= new TimeDuration(3600, TimeDuration.SECONDS);
	private static final Structure STRUCTURE = new Structure();

	static {
		STRUCTURE.addDataType(new Integer32(TIMEDURATION.getCount()));
		STRUCTURE.addDataType(new Integer32(TIMEDURATION.getTimeUnitCode()));
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.util.AXDRTimeDuration#encode(com.energyict.cbo.TimeDuration)}.
	 */
	@Test
	public final void testEncode() {
		assertNull(AXDRTimeDuration.encode(null));
		assertNotNull(AXDRTimeDuration.encode(TIMEDURATION));
		assertEquals(STRUCTURE.toString(), AXDRTimeDuration.encode(TIMEDURATION).toString());
		assertEquals(STRUCTURE.toString(), AXDRTimeDuration.encode(AXDRTimeDuration.decode(STRUCTURE)).toString());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.util.AXDRTimeDuration#decode(com.energyict.dlms.axrdencoding.AbstractDataType)}.
	 */
	@Test
	public final void testDecode() {
		assertNull(AXDRTimeDuration.decode(null));
		assertNull(AXDRTimeDuration.decode(new Integer8(0)));
		assertNotNull(AXDRTimeDuration.decode(STRUCTURE));
		assertEquals(TIMEDURATION, AXDRTimeDuration.decode(STRUCTURE));
		assertEquals(TIMEDURATION, AXDRTimeDuration.decode(AXDRTimeDuration.encode(TIMEDURATION)));
	}

}
