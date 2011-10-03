/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220.profile;


import org.junit.*;

import static org.junit.Assert.assertEquals;

/**
 * @author gna
 * @since 10-mrt-2010
 *
 */
public class DL220MeterEventListTest {

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

	@Test
	public final void convertStringEventIdToIntegerTest(){
		String strEventId = "0x8302";
		assertEquals(33538, DL220MeterEventList.convertStringEventIdToInteger(strEventId));
	}
}
