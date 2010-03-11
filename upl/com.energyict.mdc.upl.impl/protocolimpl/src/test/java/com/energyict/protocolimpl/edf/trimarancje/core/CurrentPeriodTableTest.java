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
public class CurrentPeriodTableTest {

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
			File file = new File(CurrentPeriodTable.class.getClassLoader().getResource(
					"com/energyict/protocolimpl/edf/trimarancje/core/CurrentPeriodTable_0.bin").getFile());
			FileInputStream fis = new FileInputStream(file);
			byte[] content = new byte[(int) file.length()];
			fis.read(content);
			fis.close();

			CurrentPeriodTable cpt = new CurrentPeriodTable(null);
			cpt.setCurrentTime(Long.valueOf("1268303077000"));	// set the current time somewhere in 2010, otherwise the test will fail next year
			cpt.parse(content);
			assertEquals(new Date(Long.valueOf("1267657140000")), cpt.getTimeStamp());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

}
