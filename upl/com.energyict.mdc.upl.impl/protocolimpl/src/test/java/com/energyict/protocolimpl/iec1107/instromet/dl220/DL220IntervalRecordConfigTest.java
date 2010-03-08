/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220;


import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author gna
 * @since 8-mrt-2010
 *
 */
public class DL220IntervalRecordConfigTest {

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
	
	/**
	 * Test the parsing
	 */
	@Test
	public final void parseTest(){
		String recordConfig = "(GONr)(AONr)(Zeit)(V1.G)(V1.P)(St.1)(StSy)(Er)(Check)";
		DL220IntervalRecordConfig dirc = new DL220IntervalRecordConfig(recordConfig);
		assertEquals(2, dirc.getTimeIndex());
		assertEquals(3, dirc.getValueIndex());
	}

}
