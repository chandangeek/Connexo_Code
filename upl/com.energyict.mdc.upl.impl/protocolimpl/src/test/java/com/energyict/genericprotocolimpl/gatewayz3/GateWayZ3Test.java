package com.energyict.genericprotocolimpl.gatewayz3;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
			e.printStackTrace();
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
//		al.add("28.0.5.5b");
//		al.add("28.0.5.52");
//		al.add("28.0.5.4d");
//		al.add("28.0.5.48");
//		al.add("28.0.5.49");
//		al.add("0.0.0.0");
		return al;
	}
	
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
}
