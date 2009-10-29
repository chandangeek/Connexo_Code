package com.energyict.genericprotocolimpl.gatewayz3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.energyict.cbo.BusinessException;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.mdw.core.CommunicationProfile;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.ModemPool;
import com.energyict.mdw.core.Rtu;
import com.energyict.mdw.core.RtuType;
import com.energyict.mdw.shadow.RtuShadow;
import com.energyict.mdw.testutils.RtuCRUD;
import com.energyict.mdw.testutils.RtuTypeCRUD;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocolimpl.utils.DummyDLMSConnection;
import com.energyict.protocolimpl.utils.Utilities;


public class UpdatePostDialCommandTest {
	
	private static Logger logger;
	private static GateWayZ3 gatewayZ3;
	private static DummyDLMSConnection connection;
	private static Rtu rtu;
	private static RtuType rtuType;
	private static String rtuName = "";
	private static String rtyTypeName = "";
	private ModemPool mp;
	private CommunicationProfile commProfile;
	
	@BeforeClass
	public static void setUpOnce() throws BusinessException, SQLException {
		Utilities.createEnvironment();
		MeteringWarehouse.createBatchContext(false);
		logger = Logger.getLogger("global");
		connection = new DummyDLMSConnection();
		
		gatewayZ3 = new GateWayZ3();
		gatewayZ3.setLogger(logger);
		gatewayZ3.setDLMSConnection(connection);
		gatewayZ3.setCosemObjectFactory(new CosemObjectFactory(gatewayZ3));
	}
	
	@Before
	public void setUp(){
		try {
			
			rtyTypeName = "RtuTypeName" + System.currentTimeMillis();
			rtuName = "RtuName" + System.currentTimeMillis();
			rtuType = RtuTypeCRUD.findOrCreateRtuType(rtyTypeName, 0);
			rtu = RtuCRUD.findOrCreateRtu(rtuType, rtuName, 900);
			
		} catch (BusinessException e) {
			e.printStackTrace();
			fail();
		} catch (SQLException e) {
			e.printStackTrace();
			fail();
		}
	}
	@After
	public void tearDown() throws Exception {
		RtuCRUD.deleteRtu(rtuName);
		RtuTypeCRUD.deleteRtuType(rtyTypeName);
	}
	
	@Test
	public void updateR2WithPostDialCommandRfClient(){
		int counter = 0;
		try {
			String deviceId = "28000044";
			String testPostDialCommand = "TestPostDialCommand";
			String secondPostDialCommand = "<ESC>rfclient=\"\"<\\ESC>";
			String expectedPostDialCommand = "<ESC>rfclient=\"28000044\"<\\ESC>";

			setDeviceId(deviceId);
			gatewayZ3.updateR2WithPostDialCommandRfClient(rtu);
			assertTrue(rtu.getPostDialCommand().equalsIgnoreCase(expectedPostDialCommand));
			counter++;
			
			setPostDialCommand(testPostDialCommand);
			gatewayZ3.updateR2WithPostDialCommandRfClient(rtu);
			assertTrue(rtu.getPostDialCommand().equalsIgnoreCase(testPostDialCommand));
			counter++;
			
			setPostDialCommand(secondPostDialCommand);
			gatewayZ3.updateR2WithPostDialCommandRfClient(rtu);
			assertTrue(rtu.getPostDialCommand().equalsIgnoreCase(expectedPostDialCommand));
			counter++;

			setPostDialCommand(null);
			setDeviceId(null);
			gatewayZ3.updateR2WithPostDialCommandRfClient(rtu);
			
		} catch (SQLException e) {
			e.printStackTrace();
			fail();
		} catch (BusinessException e) {
			e.printStackTrace();
			fail();
		} catch (InvalidPropertyException e) {
			if(counter == 3){	// then it is supposed to fail
				assertEquals(Integer.valueOf(3), Integer.valueOf(counter));
			} else {
				e.printStackTrace();
				fail();
			}
		}
	}
	
	private void setDeviceId(String deviceId) throws SQLException, BusinessException{
		RtuShadow shadow = rtu.getShadow();
		shadow.setDeviceId(deviceId);
		rtu.update(shadow);
	}
	
	private void setPostDialCommand(String postDialCommand) throws SQLException, BusinessException{
		RtuShadow shadow = rtu.getShadow();
		shadow.setPostDialCommand(postDialCommand);
		rtu.update(shadow);
	}
}
