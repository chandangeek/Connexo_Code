/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220;


import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author gna
 * @since 8-mrt-2010
 *
 */
public class DL220IntervalRecordTest {

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
	 * Test the intervalRecord getters
	 */
	@Test
	public void intervalRecordTest(){
		String record = "(8488)(504)(2010-03-07,09:15:00)(130)(130)(0)(15)(0x8105)(CRC Ok)";
		String recordConfig = "(GONr)(AONr)(Zeit)(V1.G)(V1.P)(St.1)(StSy)(Er)(Check)";
		DL220IntervalRecordConfig dirc = new DL220IntervalRecordConfig(recordConfig);
		DL220IntervalRecord dir = new DL220IntervalRecord(record, dirc);
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		cal.setTimeInMillis(Long.valueOf("1267953300000"));
		assertEquals(cal.getTime(), dir.getEndTime());
		assertEquals("130", dir.getValue());
	}
}
