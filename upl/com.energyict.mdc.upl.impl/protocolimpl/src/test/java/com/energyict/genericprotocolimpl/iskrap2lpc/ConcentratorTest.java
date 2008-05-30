package com.energyict.genericprotocolimpl.iskrap2lpc;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import javax.xml.rpc.ServiceException;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.energyict.cbo.BusinessException;
import com.energyict.cpo.BusinessObject;
import com.energyict.utils.Utilities;
import com.energyict.mdw.core.CommunicationProfile;
import com.energyict.mdw.core.CommunicationProtocol;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.Rtu;
import com.energyict.mdw.core.RtuType;
import com.energyict.mdw.shadow.RtuShadow;
import com.energyict.protocol.InvalidPropertyException;

public class ConcentratorTest{

	private final static String concentratorRtuType = "Iskra concentrator";
	private final static String iskraGprsMeterRtuType = "Iskra Electricity Meter [ch4]";
	
	private CommunicationProfile commProfile;
	private CommunicationScheduler scheduler;
	private Concentrator iskraConcentrator;
	private Rtu meter;
	private Rtu concentrator;
	
	private RtuType conRtuType;
	private RtuType meterRtuType;
	private RtuShadow conShadow;
	private RtuShadow meterShadow;
	
	private boolean notCorrect = false;
	
	@BeforeClass
	public static void setUpOnce() {
//		Utilities.createEnvironment();
//		MeteringWarehouse.createBatchContext(false);
	}
	
	@Before
	public void setUp() throws Exception {
//		iskraConcentrator = new Concentrator();
//		iskraConcentrator.setTESTING(true);
//		conRtuType 		= iskraConcentrator.mw().getRtuTypeFactory().find(concentratorRtuType);
//		meterRtuType	= iskraConcentrator.mw().getRtuTypeFactory().find(iskraGprsMeterRtuType);
//		conShadow 		= conRtuType.newRtuShadow();
//		meterShadow 	= meterRtuType.newRtuShadow();
//		meterShadow.setName("testRtu");
//		meter 			= iskraConcentrator.mw().getRtuFactory().create(meterShadow);
//		concentrator 	= meter.getGateway();
//		if (concentrator != null){
//			Iterator scheduleIt = concentrator.getCommunicationSchedulers().iterator();
//			while((!notCorrect) && (scheduleIt.hasNext())){
//				scheduler = (CommunicationScheduler) scheduleIt.next();
//				notCorrect = scheduler.getCommunicationProfile().getReadDemandValues();
//			}
//			iskraConcentrator.communicationProfile = scheduler.getCommunicationProfile();
//		}
	}

	@After
	public void tearDown() throws Exception {
		if(meter != null)
			meter.delete();
	}
	
	@Ignore
	@Test
	public void importProfileTest() {
		try {
			if(meter==null)
				fail();
			XmlHandler xmlHandler = new XmlHandler(null, iskraConcentrator.getChannelMap(meter));
			iskraConcentrator.importProfile(null, meter, xmlHandler);
			assertEquals(true, true);
			
		} catch (InvalidPropertyException e) {
			fail();
			e.printStackTrace();
		} catch (ServiceException e) {
			fail();
			e.printStackTrace();
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		} catch (BusinessException e) {
			fail();
			e.printStackTrace();
		}
	}
}
