/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220.profile;


import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.energyict.protocolimpl.iec1107.instromet.dl220.profile.DL220MeterEventList;

/**
 * @author gna
 * @since 10-mrt-2010
 *
 */
public class DL220MeterEventListTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
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
