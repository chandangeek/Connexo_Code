package com.energyict.genericprotocolimpl.gatewayz3;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Test;

import com.energyict.cbo.BusinessException;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.protocolimpl.utils.DummyDLMSConnection;


public class GateWayZ3Test {
	
	private static Logger logger;
	private static GateWayZ3 gatewayZ3;
	private static DummyDLMSConnection connection;
	
	private static String networkTopologyResponse = "100042c401420001050202062800055b06800000000202062800055206800000000202062800054d0680000000020206280005480680000000020206280005490680000000";
	private static String networkTopologyResponse2 = "100066c401420001080202062800055206012000000202062800054d06823800000202062800054b06023800000202062800054806022c00000202062800055b068238000" +
			"00202062800054906822c00000202062800055006832f80000202062800055106032f8000";
	private static String networkTopologyResponse3 = "100066c40142000108020206280005510681e000000202062800054d0603f980000202062800054b0682f800000202062800055b0602f80000020206280005490682f0000" +
			"0020206280005480602f00000020206280005520601e00000020206280005500682f00000";
		
	
	@BeforeClass
	public static void setUpOnce() throws BusinessException, SQLException {
		logger = Logger.getLogger("global");
		connection = new DummyDLMSConnection();
		
		gatewayZ3 = new GateWayZ3();
		gatewayZ3.setLogger(logger);
		gatewayZ3.setDLMSConnection(connection);
		gatewayZ3.setCosemObjectFactory(new CosemObjectFactory(gatewayZ3));
	}
	
	@Test
	public void getSlaveDevicesTest(){
		
		try {
			
			List<String> expectedList = getExpectedList();
			
			connection.setResponseByte(DLMSUtils.hexStringToByteArray(networkTopologyResponse));
			List<String> slaves = gatewayZ3.getSlaveDevices();

			assertArrayEquals(expectedList.toArray(), slaves.toArray());

			expectedList = getExpectedList2();
			
			connection.setResponseByte(DLMSUtils.hexStringToByteArray(networkTopologyResponse2));
			slaves = gatewayZ3.getSlaveDevices();

			assertArrayEquals(expectedList.toArray(), slaves.toArray());
			
		} catch (IOException e) {
			logger.log(Level.FINEST, e.getMessage());
			fail();
		}
	}
	
	/**
	 * @return the expected list
	 */
	private ArrayList<String> getExpectedList(){
		ArrayList<String> al = new ArrayList<String>();
		al.add("2800055b");
		al.add("28000552");
		al.add("2800054d");
		al.add("28000548");
		al.add("28000549");
		al.add("00000000");
		return al;
	}
	
	/**
	 * @return another expected list
	 */
	private ArrayList<String> getExpectedList2(){
		ArrayList<String> al = new ArrayList<String>();
		al.add("00000000");
		al.add("28000552");
		al.add("28000549");
		al.add("28000548");
		al.add("28000550");
		al.add("28000551");
		al.add("2800054d");
		al.add("2800055b");
		al.add("2800054b");
		return al;
	}
	
	/**
	 * Request the master from a given slave.
	 * GetSuperNets are added in the communicationDriver the help the subMasters get there Masters
	 */
	@Test
	public void getDeviceIfFromMasterTest(){
		
		try {
			connection.setResponseByte(DLMSUtils.hexStringToByteArray(networkTopologyResponse3));
			List<String> slaves = gatewayZ3.getSlaveDevices();	//  just need to set them
			
			assertEquals("28000552",gatewayZ3.getDeviceIdFromMaster(slaves.get(1)));	//slave 28000551
			assertEquals("00000000",gatewayZ3.getDeviceIdFromMaster(slaves.get(2)));	//slave 28000552
			assertEquals("28000548",gatewayZ3.getDeviceIdFromMaster(slaves.get(3)));	//slave 28000549
			assertEquals("28000548",gatewayZ3.getDeviceIdFromMaster(slaves.get(4)));	//slave 28000550
			assertEquals("28000552",gatewayZ3.getDeviceIdFromMaster(slaves.get(5)));	//slave 28000548
			assertEquals("2800055b",gatewayZ3.getDeviceIdFromMaster(slaves.get(6)));	//slave 2800054b
			assertEquals("28000552",gatewayZ3.getDeviceIdFromMaster(slaves.get(7)));	//slave 2800055b
			assertEquals("2800055b",gatewayZ3.getDeviceIdFromMaster(slaves.get(8)));	//slave 2800054d
			
			
		} catch (IOException e) {
			logger.log(Level.FINEST, e.getMessage());
			
		}
	}
}
