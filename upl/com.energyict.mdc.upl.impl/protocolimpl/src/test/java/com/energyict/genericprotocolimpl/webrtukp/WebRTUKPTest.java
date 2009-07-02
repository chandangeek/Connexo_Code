package com.energyict.genericprotocolimpl.webrtukp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.genericprotocolimpl.common.StoreObject;
import com.energyict.mdw.amr.RtuRegisterGroup;
import com.energyict.mdw.amr.RtuRegisterMapping;
import com.energyict.mdw.amr.RtuRegisterSpec;
import com.energyict.mdw.core.CommunicationProfile;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.ModemPool;
import com.energyict.mdw.core.Phenomenon;
import com.energyict.mdw.core.ProductSpec;
import com.energyict.mdw.core.Rtu;
import com.energyict.mdw.core.RtuType;
import com.energyict.mdw.core.TimeOfUse;
import com.energyict.mdw.testutils.CommunicationProfileCRUD;
import com.energyict.mdw.testutils.CommunicationSchedulerCRUD;
import com.energyict.mdw.testutils.ModemPoolCRUD;
import com.energyict.mdw.testutils.PhenomenonCRUD;
import com.energyict.mdw.testutils.ProductSpecCRUD;
import com.energyict.mdw.testutils.RtuCRUD;
import com.energyict.mdw.testutils.RtuRegisterGroupCRUD;
import com.energyict.mdw.testutils.RtuRegisterMappingCRUD;
import com.energyict.mdw.testutils.RtuRegisterSpecCRUD;
import com.energyict.mdw.testutils.RtuTypeCRUD;
import com.energyict.mdw.testutils.TimeOfUseCRUD;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.DummyDLMSConnection;
import com.energyict.protocolimpl.utils.Utilities;

public class WebRTUKPTest {

	private static Logger logger;

	private static WebRTUKP webRtu;
	private static DummyDLMSConnection connection;
	private static StoreObject storeObject;

	private static String regGroupName = "";
	private static String rtuRegisterMappingName = "";
	private static String rtuRegisterMappingName2 = "";
	private static String timeOfUseName = "";
	private static String phenomenonName = "";
	private static String rtuTypeName = "";
	private static String modemPoolName = "";
	private static String commProfileName = "";
	private static ObisCode OC1 = ObisCode.fromString("0.0.13.0.0.255");
	private static ObisCode OC2 = ObisCode.fromString("1.0.0.2.0.255");
	private final static Unit PHENOMENON_UNIT = Unit.get("kWh");
	
	private Rtu rtu;
	private String rtuName = "";
	private ModemPool mp;
	private CommunicationProfile commProfile;

	@BeforeClass
	public static void setUpOnce() throws BusinessException, SQLException {
		Utilities.createEnvironment();
		MeteringWarehouse.createBatchContext(false);
		logger = Logger.getLogger("global");

		webRtu = new WebRTUKP();
		connection = new DummyDLMSConnection();
		storeObject = new StoreObject();
		webRtu.setLogger(logger);
		
		/* create unique names */
		timeOfUseName = "TimeOfUseName" + System.currentTimeMillis();
		phenomenonName = "PhenomenonName" + System.currentTimeMillis();
		regGroupName = "RtuRegisterGroupName" + System.currentTimeMillis();
		rtuRegisterMappingName = "RtuRegisterName" + System.currentTimeMillis();
		rtuRegisterMappingName2 = "RtuRegisterName2" + System.currentTimeMillis();
		rtuTypeName = "RtuTypeName" + System.currentTimeMillis();
		modemPoolName = "ModemPoolName" + System.currentTimeMillis();
		commProfileName = "CommProfileName" + System.currentTimeMillis();

		/* Create the RtuType */
		RtuType rtuType = RtuTypeCRUD.findOrCreateRtuType(rtuTypeName, 2);

		/* Create timeOfUse */
		TimeOfUse timeOfUse = TimeOfUseCRUD.createTimeOfUse(timeOfUseName, 0,
				"All Day", false);

		/* Create phenomenon */
		Phenomenon phenomenon = PhenomenonCRUD.createPhenomenon(phenomenonName,
				PHENOMENON_UNIT, null, false);

		/* Create productSpec */
		ProductSpec productSpec = ProductSpecCRUD.createProductSpec(phenomenon,
				timeOfUse, false);

		/* Create the rtuRegisterGroup */
		RtuRegisterGroup rtuRegGroup = RtuRegisterGroupCRUD
				.createRtuRegisterGroup(regGroupName);

		/* Create the rtuRegisterMapping 1 */
		RtuRegisterMapping rrm1 = RtuRegisterMappingCRUD.createRegisterMapping(
				rtuRegisterMappingName, OC1.getDescription(), false, OC1,
				productSpec.getId(), rtuRegGroup.getId());
		
		/* Create the rtuRegisterMapping 2, this one has no registergroup */
		RtuRegisterMapping rrm2 = RtuRegisterMappingCRUD.createRegisterMapping(
				rtuRegisterMappingName2, OC2.getDescription(), false, OC2,
				productSpec.getId(), 0);
		
		/* Create rtuRegisterSpec 1 */
		RtuRegisterSpec registerSpec1 = RtuRegisterSpecCRUD
				.createRtuRegisterSpec(rtuType, rrm1);
		
		/* Create rtuRegisterSpec 2 */
		RtuRegisterSpec registerSpec2 = RtuRegisterSpecCRUD
		.createRtuRegisterSpec(rtuType, rrm2);
	}

	@AfterClass
	public static void tearDownOnce() throws BusinessException, SQLException {

		/* Delete the rtuType */
		RtuTypeCRUD.deleteRtuType(rtuTypeName);
		
        /* Delete the registerMappings*/
        RtuRegisterMappingCRUD.deleteRegisterMapping(rtuRegisterMappingName);
        RtuRegisterMappingCRUD.deleteRegisterMapping(rtuRegisterMappingName2);
        
        /* Delete the registergroup */
        RtuRegisterGroupCRUD.deleteRtuRegisterGroup(regGroupName);
        
        /* Delete product spec */
        TimeOfUse timeOfUse = TimeOfUseCRUD.findTimeOfUse(timeOfUseName);
        Phenomenon phenomenon = PhenomenonCRUD.findPhenomenon(phenomenonName, PHENOMENON_UNIT);
        ProductSpecCRUD.deleteProductSpec(phenomenon, timeOfUse);
        
        /* Delete phenomenon */
        PhenomenonCRUD.deletePhenomenon(phenomenonName, PHENOMENON_UNIT);
        
        /* Delete timeOfUse */
        TimeOfUseCRUD.deleteTimeOfUse(timeOfUseName);
	}

	@Before
	public void setUp() throws Exception {
		this.rtuName = "RtuName" + System.currentTimeMillis();
		this.rtu = RtuCRUD.findOrCreateRtu(RtuTypeCRUD.findRtuType(rtuTypeName), rtuName, 900);
		
		mp = ModemPoolCRUD.findOrCreateModemPool(modemPoolName);
		webRtu.setRtu(this.rtu);
		webRtu.setDLMSConnection(connection);
		webRtu.setCosemObjectFactory(new CosemObjectFactory((ProtocolLink)webRtu));
	}

	@After
	public void tearDown() throws Exception {
		RtuCRUD.deleteRtu(rtuName);
		CommunicationProfileCRUD.deleteCommunicationProfile(commProfileName);
		ModemPoolCRUD.deleteModemPool(modemPoolName);
	}
	
	@Test
	public void registerGroupsTest(){
		
		try {
			
			connection.setResponseByte(new byte[]{(byte)0x10,(byte)0x00,(byte)0x0A,(byte)0xC4,(byte)0x01,(byte)0xC1,(byte)0x00,(byte)0x09,(byte)0x05
				,(byte)0x54,(byte)0x75,(byte)0x6D,(byte)0x6D,(byte)0x79});
			
			webRtu.setStoreObject(storeObject);
			List regGroups = new ArrayList();
			regGroups.add(RtuRegisterGroupCRUD.findRegisterGroup(regGroupName).getShadow());
			commProfile = CommunicationProfileCRUD.findOrCreateCommunicationProfile(commProfileName, true, false, false, false, false, regGroups);
			CommunicationSchedulerCRUD.createCommunicationScheduler(this.rtu, this.commProfile, this.mp);
			assertEquals(1, rtu.getCommunicationSchedulers().size());
			
			webRtu.setCommunicationScheduler((CommunicationScheduler)this.rtu.getCommunicationSchedulers().get(0)); // we should only get one!
			webRtu.doReadRegisters();
			
			assertTrue(webRtu.getStoreObject().getMap().containsKey(MeteringWarehouse.getCurrent().getRtuRegisterFactory().findByRtuRegisterSpec(
					RtuRegisterSpecCRUD.findRtuRegistSpec(this.rtu.getRtuTypeId(), RtuRegisterMappingCRUD.findRegisterMapping(rtuRegisterMappingName).getId())).get(0)));
			
			assertFalse(webRtu.getStoreObject().getMap().containsKey(MeteringWarehouse.getCurrent().getRtuRegisterFactory().findByRtuRegisterSpec(
					RtuRegisterSpecCRUD.findRtuRegistSpec(this.rtu.getRtuTypeId(), RtuRegisterMappingCRUD.findRegisterMapping(rtuRegisterMappingName2).getId())).get(0)));
			
		} catch (SQLException e) {
			e.printStackTrace();
			fail();
		} catch (BusinessException e) {
			e.printStackTrace();
			fail();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
		
	}
	
	@Test
	public void markAsBadTimeTest(){
		try{
			commProfile = CommunicationProfileCRUD.createCommunicationProfile(commProfileName,false, false, false, false, true, true, false, false, 3600, 10, null);
			CommunicationSchedulerCRUD.createCommunicationScheduler(this.rtu, this.commProfile, this.mp);
			assertEquals(1, rtu.getCommunicationSchedulers().size());
			
			webRtu.setCommunicationScheduler((CommunicationScheduler)this.rtu.getCommunicationSchedulers().get(0));
			webRtu.setMeterConfig(DLMSMeterConfig.getInstance("WKP"));
					
			/* Set the time two hours back */
			Calendar cal = Calendar.getInstance(webRtu.getTimeZone());
			cal.getTime();
			cal.add(Calendar.HOUR_OF_DAY, -2);
			AXDRDateTime dt = new AXDRDateTime(cal);
//			DateTime dt = new DateTime(cal);
			byte[] resp = new byte[]{(byte)0x10,(byte)0x00,(byte)0x0A,(byte)0xC4,(byte)0x01,(byte)0xC1,(byte)0x00,
					0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			System.arraycopy(dt.getBEREncodedByteArray(), 0, resp, 7, resp.length-7);
			connection.setResponseByte(resp);
			
			/* The timedifference is out of the boundary */
			assertTrue(webRtu.verifyMaxTimeDifference());
			
			/* Set the time to the current time */
			cal = Calendar.getInstance(webRtu.getTimeZone());
			dt = new AXDRDateTime(cal);
			resp = new byte[]{(byte)0x10,(byte)0x00,(byte)0x0A,(byte)0xC4,(byte)0x01,(byte)0xC1,(byte)0x00,
					0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			System.arraycopy(dt.getBEREncodedByteArray(), 0, resp, 7, resp.length-7);
			connection.setResponseByte(resp);
			
			/* The timedifference is in the boundary */
			assertFalse(webRtu.verifyMaxTimeDifference());
			
		} catch (BusinessException e) {
			e.printStackTrace();
			fail();
		} catch (SQLException e) {
			e.printStackTrace();
			fail();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} finally {
			
		}
	}
}
