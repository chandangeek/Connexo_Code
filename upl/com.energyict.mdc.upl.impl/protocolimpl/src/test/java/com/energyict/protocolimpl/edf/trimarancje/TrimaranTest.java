package com.energyict.protocolimpl.edf.trimarancje;

import static org.junit.Assert.*;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.energyict.cbo.Unit;
import com.energyict.cbo.Utils;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.edf.trimarancje.core.DataFactory;
import com.energyict.protocolimpl.edf.trimarancje.core.DemandData;

public class TrimaranTest {
	
	private Trimaran cje;
	private String trimaranProfile = "/offlineFiles/TrimaranProfile.bin";
	private String[] profile = {"/offlineFiles/Profile1.bin", "/offlineFiles/Profile2.bin",
								"/offlineFiles/Profile3.bin", "/offlineFiles/Profile4.bin",
								"/offlineFiles/Profile5.bin", "/offlineFiles/Profile6.bin",
								"/offlineFiles/Profile7.bin", "/offlineFiles/Profile8.bin",
								"/offlineFiles/Profile9.bin", "/offlineFiles/Profile10.bin",
								"/offlineFiles/Profile11.bin", "/offlineFiles/Profile12.bin",
								"/offlineFiles/Profile13.bin", "/offlineFiles/Profile14.bin",
								"/offlineFiles/Profile15.bin", "/offlineFiles/Profile16.bin",};
	private String test = "/offlineFiles/Profile1.bin";
	@Before
	public void setUp() throws Exception {
		cje = new Trimaran();
		cje.init(null, null, TimeZone.getTimeZone("ECT"), null);
	}

	@After
	public void tearDown() throws Exception {
		cje.release();
	}
	
	@Test
	public void profileTest(){
		try {
			cje.setTrimaranProfile(new TrimaranProfile(cje));
			cje.setDataFactory(new DataFactory(cje));
			cje.getTrimaranProfile().setDemandData(new DemandData(cje.getDataFactory()));
	        File file = new File(Utils.class.getResource(trimaranProfile).getFile());
	        FileInputStream fis = new FileInputStream(file);
	        byte[] data=new byte[(int)file.length()];
	        fis.read(data);
	        fis.close();   
	        cje.getTrimaranProfile().getDemandData().parse(data);
	        cje.getTrimaranProfile().incrementPointer();
	        
	        /* have to check two profiles now */
	        cje.getTrimaranProfile().setDemandData(new DemandData(cje.getDataFactory()));
	        cje.getTrimaranProfile().getDemandData().parse(data);
	        
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
	
	@Test
	/**
	 *  this test has to check the V2 of the Trimaran meters 
	 */
	public void profileTest2(){
		cje.setTrimaranProfile(new TrimaranProfile(cje));
		cje.setDataFactory(new DataFactory(cje));
		try {
			for(int i = 0; i < profile.length; i++){
				cje.getTrimaranProfile().setDemandData(new DemandData(cje.getDataFactory()));
				File file = new File(Utils.class.getResource(profile[i]).getFile());
				FileInputStream fis;
				fis = new FileInputStream(file);
				byte[] data=new byte[(int)file.length()];
				fis.read(data);
				fis.close();   
				cje.getTrimaranProfile().getDemandData().parse(data);
				cje.getTrimaranProfile().incrementPointer();
					
			}
			assertEquals(cje.getTrimaranProfile().getPointer(), 16);
			cje.getTrimaranProfile().setPointer(1);
			ChannelInfo ci = (ChannelInfo) cje.getTrimaranProfile().getProfileData().getChannelInfos().get(0);
			assertEquals(ci.getUnit(), Unit.get("kW"));
		} catch (FileNotFoundException e) {
			fail();
			e.printStackTrace();
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}
	}

}
