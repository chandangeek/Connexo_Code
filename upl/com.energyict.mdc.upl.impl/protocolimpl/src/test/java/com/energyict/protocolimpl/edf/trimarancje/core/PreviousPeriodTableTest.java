/**
 * 
 */
package com.energyict.protocolimpl.edf.trimarancje.core;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author gna
 * @since 11-mrt-2010
 *
 */
public class PreviousPeriodTableTest {

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
	public final void parseTest(){
		
		try {
			File file = new File(PreviousPeriodTable.class.getClassLoader().getResource(
					"com/energyict/protocolimpl/edf/trimarancje/core/PreviousPeriodTable_0.bin").getFile());
			FileInputStream fis = new FileInputStream(file);
			byte[] content = new byte[(int) file.length()];
			fis.read(content);
			fis.close();

			PreviousPeriodTable ppt = new PreviousPeriodTable(null);
			ppt.setCurrentTime(Long.valueOf("1268303077000"));	// set the current time somewhere in 2010, otherwise the test will fail next year
			ppt.parse(content);
			assertEquals(new Date(Long.valueOf("1265238000000")), ppt.getTimeStamp());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}
}
