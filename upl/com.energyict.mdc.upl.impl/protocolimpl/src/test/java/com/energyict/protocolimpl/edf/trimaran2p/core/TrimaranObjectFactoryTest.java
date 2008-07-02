package com.energyict.protocolimpl.edf.trimaran2p.core;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.cbo.Utils;
import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.LinkException;
import com.energyict.protocolimpl.edf.trimaran2p.Trimaran2P;
import com.energyict.protocolimpl.edf.trimarandlms.axdr.DataContainer;
import com.energyict.protocolimpl.edf.trimarandlms.dlmscore.APSEPDUFactory;
import com.energyict.protocolimpl.edf.trimarandlms.dlmscore.dlmspdu.DLMSPDUFactory;
import com.energyict.protocolimpl.edf.trimarandlms.protocol.APSEParameters;
import com.energyict.utils.Utilities;

import static org.junit.Assert.*;

public class TrimaranObjectFactoryTest {
	
	private Trimaran2P deuxP;
	private TrimaranObjectFactory tof;
	private EnergieIndexReader energieIndexReader = null;
	private EnergieIndex energieIndex = null;
	private Energies energies = null;
	
	private Quantity expected = new Quantity(new BigDecimal(BigInteger.valueOf(328499700),3), Unit.get("MWh"));

	@Before
	public void setUp() throws Exception {
		
		deuxP = new Trimaran2P();
		deuxP.init(null, null, TimeZone.getTimeZone("ECT"), null);
		tof = new TrimaranObjectFactory(deuxP);
	}

	@After
	public void tearDown() throws Exception {
	}
	
//	@Test
//	public void writeAccessPartielTest(){
//		try {
//			dialer = Utilities.getNewDialer();
//			tof.getTrimaran().doInit(dialer.getInputStream(), dialer.getOutputStream(), 0, 0, 0, 0, 0, null, dialer.getHalfDuplexController());
//			tof.getTrimaran().setDLMSPDUFactory(new DLMSPDUFactory(tof.getTrimaran()));
//			tof.getTrimaran().doValidateProperties(new Properties());
//			tof.getTrimaran().setAPSEFactory(new APSEPDUFactory(tof.getTrimaran(), tof.getTrimaran().getAPSEParameters()));
//			accessPartiel = new AccessPartiel(tof);
//			accessPartiel.setDateAccess(Calendar.getInstance().getTime());
//			accessPartiel.setNomAccess(1);
//			accessPartiel.write();
//		} catch (IOException e) {
//			e.printStackTrace();
//			fail();
//		} catch (LinkException e) {
//			e.printStackTrace();
//			fail();
//		}
//	}
	
	@Test
	public void readEnergyIndexTest() {
		File file;
		FileInputStream fis;
		DataContainer dc = new DataContainer();
		try {
			
			file = new File(Utils.class.getResource("/offlineFiles/trimaran/EnergieIndexes.bin").getFile());
			fis = new FileInputStream(file);
			byte[] data=new byte[(int)file.length()];
			fis.read(data);
			fis.close();
			
			dc.parseObjectList(data, tof.getTrimaran().getLogger());
			
			energies = new Energies(dc, tof.getTrimaran().getTimeZone(), 56);
			energieIndexReader = new EnergieIndexReader(tof);
			energieIndexReader.setEnergie(energies);
			energieIndexReader.setVariableName(56);
			energieIndex = new EnergieIndex();
			energieIndex.addEnergie(energieIndexReader.getEnergie());
			
			if(energieIndex.energies.size() > 1)
				fail();
			Quantity testIxNRJact = energieIndex.getEnergie(56).getIxNRJact(0).add(energieIndex.getEnergie(56).getNRJact_Reste(0));
			assertEquals(expected.getAmount(), testIxNRJact.getAmount());
			
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

}
