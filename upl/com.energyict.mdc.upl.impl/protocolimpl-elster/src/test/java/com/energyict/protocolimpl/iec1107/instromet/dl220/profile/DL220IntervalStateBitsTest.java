/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220.profile;


import com.energyict.protocol.IntervalStateBits;
import org.junit.*;

import static org.junit.Assert.assertEquals;

/**
 * @author gna
 * @since 9-mrt-2010
 *
 */
public class DL220IntervalStateBitsTest {

	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
	/**
	 * Test multiple statusses
	 */
	@Test
	public final void intervalStatusBitsTest(){
		String status = "14";
		assertEquals(IntervalStateBits.SHORTLONG, DL220IntervalStateBits.intervalStateBits(status));
		status = "7;8;9";
		assertEquals(IntervalStateBits.DEVICE_ERROR| IntervalStateBits.CONFIGURATIONCHANGE| IntervalStateBits.BATTERY_LOW, DL220IntervalStateBits.intervalStateBits(status));
		
	}

}
