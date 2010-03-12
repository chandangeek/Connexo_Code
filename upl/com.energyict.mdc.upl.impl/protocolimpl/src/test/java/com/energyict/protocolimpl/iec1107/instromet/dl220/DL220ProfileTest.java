/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalValue;

/**
 * @author gna
 * @since 8-mrt-2010
 *
 */
public class DL220ProfileTest {

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
	 * Test the construction of the intervalData
	 */
	@Test
	public final void buildIntervalDataTest(){
		try {
			DL220 link = new DL220();
			link.init(null, null, TimeZone.getTimeZone("GMT"), Logger.getAnonymousLogger());
			File file = new File(DL220Profile.class.getClassLoader().getResource(
			"com/energyict/protocolimpl/iec1107/instromet/dl220/DL220Profile.bin").getFile());
			FileInputStream fis = new FileInputStream(file);
			byte[] content = new byte[(int) file.length()];
			fis.read(content);
			fis.close();
			String recordConfig = "(GONr)(AONr)(Zeit)(V1.G)(V1.P)(St.1)(StSy)(Er)(Check)";
			DL220Profile dlProfile = new DL220Profile(link, 0, Archives.MEASUREMENT1, 10);
			dlProfile.setCapturedObjects(recordConfig);
			dlProfile.setInterval(300);
			DL220RecordConfig dirc = new DL220IntervalRecordConfig(recordConfig);
			dlProfile.setDirc(dirc);
			List<IntervalData> intervalData = dlProfile.buildIntervalData(new String(content));
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
			cal.setTimeInMillis(Long.valueOf("1268041200000"));
			
			assertEquals(cal.getTime(), intervalData.get(299).getEndTime());
			assertEquals(new Integer(172), ((IntervalValue)intervalData.get(299).getIntervalValues().get(0)).getNumber());
			
			cal.setTimeInMillis(Long.valueOf("1268039700000"));
			assertEquals(cal.getTime(), intervalData.get(294).getEndTime());
			assertEquals(new Integer(164), ((IntervalValue)intervalData.get(294).getIntervalValues().get(0)).getNumber());
			
			cal.setTimeInMillis(Long.valueOf("1268039100000"));
			assertEquals(cal.getTime(), intervalData.get(292).getEndTime());
			assertEquals(new Integer(159), ((IntervalValue)intervalData.get(292).getIntervalValues().get(0)).getNumber());			
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public final void sortOutIntervalListTest(){
		List<IntervalData> intervalList = new ArrayList<IntervalData>();
		DL220Profile dlProfile = new DL220Profile(null, 0, Archives.MEASUREMENT1, 10);
		dlProfile.setInterval(300);
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		cal.setTimeInMillis(Long.valueOf("1268121900000"));
	
		// **** build some intervalData ****
		IntervalData id = new IntervalData(cal.getTime());
		id.addValue(100);
		intervalList.add(id);
		
		// add 2 minutes to the calendar
		cal.add(Calendar.MINUTE, 2);
		id = new IntervalData(cal.getTime());
		id.addValue(120);
		intervalList.add(id);
		
		// add 3 minutes to get to a boundary
		cal.add(Calendar.MINUTE, 3);
		id = new IntervalData(cal.getTime());
		id.addValue(150);
		intervalList.add(id);
		
		// add a couple of milliseconds to see if he skips these to
		cal.add(Calendar.MILLISECOND, 200);
		id = new IntervalData(cal.getTime());
		id.addValue(new BigDecimal(new BigInteger("1502"), 1));
		intervalList.add(id);
		// *********************************
		
		try {
			assertEquals(4, intervalList.size());			// should always be true
			List<IntervalData> idList = dlProfile.sortOutIntervalList(intervalList);
			assertEquals(2, idList.size());
			assertEquals(100, ((IntervalValue)idList.get(0).getIntervalValues().get(0)).getNumber());
			assertEquals(150, ((IntervalValue)idList.get(1).getIntervalValues().get(0)).getNumber());
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}
}
