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
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

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
			File file = new File(DL220Profile.class.getClassLoader().getResource(
			"com/energyict/protocolimpl/iec1107/instromet/dl220/DL220Profile.bin").getFile());
			FileInputStream fis = new FileInputStream(file);
			byte[] content = new byte[(int) file.length()];
			fis.read(content);
			fis.close();
			String recordConfig = "(GONr)(AONr)(Zeit)(V1.G)(V1.P)(St.1)(StSy)(Er)(Check)";
			DL220Profile dlProfile = new DL220Profile(null, 0, Archives.MEASUREMENT1, 10);
			DL220IntervalRecordConfig dirc = new DL220IntervalRecordConfig(recordConfig);
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
	
}
