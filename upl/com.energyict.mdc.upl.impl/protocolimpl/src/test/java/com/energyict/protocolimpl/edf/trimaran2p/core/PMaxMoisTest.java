/**
 * 
 */
package com.energyict.protocolimpl.edf.trimaran2p.core;


import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.energyict.cbo.Utils;
import com.energyict.protocolimpl.edf.trimaran2p.Trimaran2P;
import com.energyict.protocolimpl.edf.trimarandlms.axdr.DataContainer;

/**
 * @author gna
 *
 */
public class PMaxMoisTest {
	
	private Trimaran2P deuxP;
	FileInputStream fis;
	File file;
	DataContainer dc;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		deuxP = new Trimaran2P();
		deuxP.init(null, null, TimeZone.getTimeZone("ECT"), null);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		deuxP.release();
		fis.close();
	}
	
	@Ignore
	@Test
	public void parseTest(){
		PMaxMois pmaxMois = new PMaxMois(deuxP.getTrimaranObjectFactory());
		
		try {
			file = new File(Utils.class.getResource("/offlineFiles/trimaran/deuxp857/089807000857PMaxMois.bin").getFile());
			fis = new FileInputStream(file);
			byte[] data=new byte[(int)file.length()];
	        fis.read(data);
	        pmaxMois.parse(data);
	        
	        
	        // has no decent values to check, they are all zero's
	        // TODO check if you get these values from other meters!
	        assertTrue(true);
	        
		} catch (FileNotFoundException e) {
			fail();
			e.printStackTrace();
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}
	}

}
