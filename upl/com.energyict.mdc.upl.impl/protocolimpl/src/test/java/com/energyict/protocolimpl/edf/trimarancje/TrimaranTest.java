package com.energyict.protocolimpl.edf.trimarancje;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.energyict.cbo.Utils;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.edf.trimarancje.core.DataFactory;
import com.energyict.protocolimpl.edf.trimarancje.core.DemandData;

public class TrimaranTest {
	
	private Trimaran cje;
	private String trimaranProfile = "/offlineFiles/TrimaranProfile.bin";

	@Before
	public void setUp() throws Exception {
		cje = new Trimaran();
		cje.init(null, null, TimeZone.getTimeZone("ECT"), null);
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void profileTest(){
		try {
			cje.setTrimeranProfile(new TrimaranProfile(cje));
			cje.setDataFactory(new DataFactory(cje));
			cje.getTrimeranProfile().setDemandData(new DemandData(cje.getDataFactory()));
	        File file = new File(Utils.class.getResource(trimaranProfile).getFile());
	        FileInputStream fis = new FileInputStream(file);
	        byte[] data=new byte[(int)file.length()];
	        fis.read(data);
	        fis.close();   
	        cje.getTrimeranProfile().getDemandData().parse(data);
			cje.getProfileData(null, null, false);
			// if we can get this far it should be OK ...
		} catch (UnsupportedException e) {
			fail();
			e.printStackTrace();
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}
	}

}
