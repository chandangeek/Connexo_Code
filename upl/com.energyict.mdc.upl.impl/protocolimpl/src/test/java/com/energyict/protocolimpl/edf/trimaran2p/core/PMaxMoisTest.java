/**
 * 
 */
package com.energyict.protocolimpl.edf.trimaran2p.core;


import com.energyict.cbo.Utils;
import com.energyict.protocolimpl.edf.trimaran2p.Trimaran2P;
import com.energyict.protocolimpl.edf.trimarandlms.axdr.TrimaranDataContainer;
import org.junit.*;

import java.io.*;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author gna
 *
 */
public class PMaxMoisTest {
	
	private Trimaran2P deuxP;
	FileInputStream fis;
	File file;
	TrimaranDataContainer dc;

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
	
	@Test
	public void parseTest(){
		PMaxMois pmaxMois = new PMaxMois(deuxP.getTrimaranObjectFactory());
		
		try {
			file = new File(Utils.class.getResource("/offlineFiles/trimaran/deuxp857/089807000857PMaxMois.bin").getFile());
			fis = new FileInputStream(file);
			byte[] data=new byte[(int)file.length()];
	        fis.read(data);
	        pmaxMois.parse(data);
	        
	        assertEquals(0, pmaxMois.getPAnMax(0).getAmount().intValue());
	        assertEquals(0, pmaxMois.getPAnMax(1).getAmount().intValue());
	        assertEquals(0, pmaxMois.getPAnMax(2).getAmount().intValue());
	        assertEquals(0, pmaxMois.getPAnMax(3).getAmount().intValue());
	        assertEquals(0, pmaxMois.getPAnMax(4).getAmount().intValue());
	        // has no decent values to check, they are all zero's
	        // TODO check if you get these values from other meters!
	        
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

}
