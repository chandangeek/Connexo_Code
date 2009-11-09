package com.energyict.genericprotocolimpl.webrtu.common;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.Unsigned8;

public class ConvertShortIdTest {

	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	
	/**
	 * Test the construction of the ShortID for MBus detection
	 */
	@Test
	public void constructShortIdTest(){
		
		MbusProvider mp = new MbusProvider(null, null);
		
		String expectedShortIdLandisGyr = "FML1000013500003";
		String expectedShortIdFlonidan = "FLO1234567806303";
		
		try {
			Unsigned8 version = new Unsigned8(DLMSUtils.hexStringToByteArray("1100"),0);
			Unsigned8 deviceType = new Unsigned8(DLMSUtils.hexStringToByteArray("1103"),0);
			Unsigned32 identificationNumber = new Unsigned32(DLMSUtils.hexStringToByteArray("0610000135"),0);
			Unsigned16 manufacturerId = new Unsigned16(DLMSUtils.hexStringToByteArray("1219ac"),0);
			
			assertEquals(expectedShortIdLandisGyr, mp.constructShortId(manufacturerId, identificationNumber, version, deviceType));
			
			version = new Unsigned8(DLMSUtils.hexStringToByteArray("113F"),0);
			deviceType = new Unsigned8(DLMSUtils.hexStringToByteArray("1103"),0);
			identificationNumber = new Unsigned32(DLMSUtils.hexStringToByteArray("0612345678"),0);
			manufacturerId = new Unsigned16(DLMSUtils.hexStringToByteArray("12198F"),0);
			
			assertEquals(expectedShortIdFlonidan, mp.constructShortId(manufacturerId, identificationNumber, version, deviceType));
			
		} catch (IOException e) {
			// should not come here, the given data is in the correct format
			fail();
		}
	}

}
