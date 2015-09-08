package com.energyict.protocolimpl.modbus.socomec.a40;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.generic.ParseUtils;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.modbus.core.connection.ModbusTestConnection;
import com.energyict.protocolimpl.modbus.core.connection.ResponseData;

public class A40Test {
	
	private static A40 a40;
	private static Logger logger;
	private static ModbusTestConnection modbusConnection;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		logger = Logger.getLogger("global");
		modbusConnection = new ModbusTestConnection();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		a40 = new A40();
		a40.setModbusConnection(modbusConnection);
		a40.setLogger(logger);
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public final void functionalTesting(){
		
		try {
			modbusConnection.setResponseData(buildResponseData("080000004000ff00ff", Integer.valueOf(3)));
			assertTrue(a40.getProfile().isSupported());
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
		
		try {
			modbusConnection.setResponseData(buildResponseData("080001000100010001", Integer.valueOf(3)));
			List<ChannelInfo> result = a40.getProfile().getChannelInfos();
			assertEquals(createExpectedChannelInfos().size(), result.size());
			
			assertEquals(createExpectedChannelInfos().get(0).getName(), result.get(0).getName());
			assertEquals(createExpectedChannelInfos().get(0).getChannelId(), result.get(0).getChannelId());
			assertEquals(createExpectedChannelInfos().get(0).getUnit(), result.get(0).getUnit());
			
			assertEquals(createExpectedChannelInfos().get(3).getName(), result.get(3).getName());
			assertEquals(createExpectedChannelInfos().get(3).getChannelId(), result.get(3).getChannelId());
			assertEquals(createExpectedChannelInfos().get(3).getUnit(), result.get(3).getUnit());
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
		
		try {
			modbusConnection.setResponseData(buildResponseData("02016e", Integer.valueOf(3)));
			assertEquals(366,a40.getProfile().getActiveEnergyPointer());
		} catch (UnsupportedException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}

		
		try {
			modbusConnection.setResponseData(buildResponseData("060b10100f0009", Integer.valueOf(3)));
			Date dateTime = a40.getProfile().getDateTimeLastProfileUpdate();
			Date expectedDate = new Date(Long.valueOf("1258388100000"));
			assertEquals(expectedDate.compareTo(dateTime), 0);
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Create an expected list of ChannelInfos
	 * @return the constructed list of channelinfos
	 */
	private List<ChannelInfo> createExpectedChannelInfos(){
		List<ChannelInfo> result = new ArrayList<ChannelInfo>();
		result.add(new ChannelInfo(0,0,"0.1.128.0.0.255", Unit.get(BaseUnit.WATT, -1)));
		result.add(new ChannelInfo(1,1,"0.2.128.0.0.255", Unit.get(BaseUnit.WATT, -1)));
		result.add(new ChannelInfo(2,2,"0.3.128.0.0.255", Unit.get(BaseUnit.VOLTAMPEREREACTIVE, -1)));
		result.add(new ChannelInfo(3,3,"0.4.128.0.0.255", Unit.get(BaseUnit.VOLTAMPEREREACTIVE, -1)));
		return result;
	}
	
	/**
	 * Create a ResponseData object by using the given parameters
	 * @param data the data to set
	 * @param functionCode the function code to set 
	 * @return the constructed ResponseData
	 */
	private ResponseData buildResponseData(String data, int functionCode){
		return buildResponseData(ParseUtils.hexStringToByteArray(data), functionCode);
	}

	/**
	 * Create a ResponseData object by using the given parameters
	 * @param data the data to set
	 * @param functionCode the function code to set 
	 * @return the constructed ResponseData
	 */
	private ResponseData buildResponseData(byte[] data, int functionCode){
		ResponseData rd = new ResponseData();
		rd.setData(data);
		rd.setFunctionCode(functionCode);
		rd.setAddress(258);
		return rd;
	}
}
