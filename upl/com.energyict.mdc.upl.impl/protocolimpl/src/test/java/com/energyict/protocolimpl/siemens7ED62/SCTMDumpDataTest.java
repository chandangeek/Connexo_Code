/**
 * 
 */
package com.energyict.protocolimpl.siemens7ED62;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

/**
 * @author gna
 *
 */
public class SCTMDumpDataTest {


	@Test
	public final void getBillingCounterTest(){
		try {
			File file = new File(SCTMDumpData.class.getClassLoader().getResource("com/energyict/protocolimpl/siemens7ED62/dumpDataByteArray.bin").getFile());
			FileInputStream fis = new FileInputStream(file);
			byte[] content = new byte[(int) file.length()];
			fis.read(content);
		
			SCTMDumpData dumpData = new SCTMDumpData(content,0);
			assertEquals(10, dumpData.getBillingCounter());
			
			/* Try to simulate the OLD method */
			file = new File(SCTMDumpData.class.getClassLoader().getResource("com/energyict/protocolimpl/siemens7ED62/dumpDataByteArray2.bin").getFile());
			fis = new FileInputStream(file);
			content = new byte[(int) file.length()];
			fis.read(content);
			
			dumpData = new SCTMDumpData(content,0);
			assertEquals(99, dumpData.getBillingCounter());
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}
}
