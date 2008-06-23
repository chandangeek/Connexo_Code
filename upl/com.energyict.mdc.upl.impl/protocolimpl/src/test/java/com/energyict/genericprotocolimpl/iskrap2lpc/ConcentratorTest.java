package com.energyict.genericprotocolimpl.iskrap2lpc;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Unit;
import com.energyict.mdw.core.CommunicationProtocol;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.Rtu;
import com.energyict.mdw.core.RtuType;
import com.energyict.mdw.coreimpl.CommunicationProtocolImpl;
import com.energyict.mdw.coreimpl.RtuTypeImpl;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalValue;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.utils.Utilities;

public class ConcentratorTest{

	private static Logger logger;
	
	private String jcnConcentrator = "com.energyict.genericprotocolimpl.iskrap2lpc.Concentrator";
	private String jcnIskraMeter = "com.energyict.genericprotocolimpl.iskrap2lpc.Meter";
//	private String jcnIskraMeter = "com.energyict.genericprotocolimpl.iskrap2lpc.JustATest";
	private String testMeter = "TestMeter";
	
	private CommunicationProtocol commProtMeter = null;
	private Concentrator iskraConcentrator;
	private MeterReadTransaction meterReadTransaction;
	private Rtu meter;
	private RtuType rtuTypeMeter = null;
	
	private List result = new ArrayList();
	
	@BeforeClass
	public static void setUpOnce() {
		Utilities.createEnvironment();
		MeteringWarehouse.createBatchContext(false);
		logger = Logger.getLogger("testLogger");
	}
	
	@Before
	public void setUp() throws Exception {
		iskraConcentrator = new Concentrator();
		iskraConcentrator.setTESTING(true);
		meterReadTransaction = new MeterReadTransaction(iskraConcentrator, null, null, null);
		meterReadTransaction.setTESTING(true);
		
		// find out if the communication profile exists, if not, create it
		result = Utilities.mw().getCommunicationProtocolFactory().findAll();
		for(int i = 0; i < result.size(); i++){
			if(((CommunicationProtocol)result.get(i)).getJavaClassName().equalsIgnoreCase(jcnIskraMeter)){
				commProtMeter = (CommunicationProtocol)result.get(i);
				break;
			}
		}
		if(commProtMeter == null)
			commProtMeter = Utilities.createCommunicationProtocol(jcnIskraMeter);
		
		// find out if there is an rtuType defined with this testName, it not, create it
		result = Utilities.mw().getRtuTypeFactory().findByName(testMeter);
		if(result.size() == 0)
			rtuTypeMeter = Utilities.createRtuType(commProtMeter, testMeter, 4);
		else
			rtuTypeMeter = (RtuType)result.get(0);
		
	}

	@After
	public void tearDown() throws Exception {
		// first delete the device
		List result = Utilities.mw().getRtuFactory().findByName(testMeter);
		if (result.size() > 0)
			for(int i = 0; i < result.size(); i++)
				((Rtu)result.get(0)).delete();
		
		// then the deviceType
		result = Utilities.mw().getRtuTypeFactory().findByName(testMeter);
		if (result.size() > 0)
			for(int i = 0; i < result.size(); i++)
				((RtuTypeImpl)result.get(0)).delete();
		
		// then the communication profile
		result = Utilities.mw().getCommunicationProtocolFactory().findByName(jcnIskraMeter);
		if (result.size() > 0)
			for(int i = 0; i < result.size(); i++)
				((CommunicationProtocolImpl)result.get(0)).delete();
	}
	
	@Ignore
	@Test
	public void importProfileTest() {
		try {
			Number firstNumber = 0.228;
			Number lastNumber = 21.553;
			Number someNumber = 0.892;
			
			// find out if there is already a meter with the TestMeter name, if not, create it
			result = Utilities.mw().getRtuFactory().findByName(testMeter);
			if(result.size() == 0)
				meter = Utilities.createRtu(rtuTypeMeter, "12345678", 900);
			else
				meter = (Rtu)result.get(0);
			
			if(meter==null)
				fail();
			
			meter = Utilities.addPropertyToRtu(meter, Constant.CHANNEL_MAP, "1.8.0+9:2.8.0+9:0.1.128.50.0+9");
			iskraConcentrator.setLogger(logger);
			
			XmlHandler xmlHandler = new XmlHandler(logger, meterReadTransaction.getChannelMap(meter));
			meterReadTransaction.importProfile(null, meter, xmlHandler, false);
			
			assertEquals(2, xmlHandler.getProfileData().length);
			assertEquals(Unit.get("kWh"), ((ChannelInfo)xmlHandler.getProfileData()[0].getChannelInfos().get(0)).getUnit());
			assertEquals("ELECTRICITY", ((ChannelInfo)xmlHandler.getProfileData()[0].getChannelInfos().get(0)).getName());
			assertEquals(Unit.get("m3"), ((ChannelInfo)xmlHandler.getProfileData()[1].getChannelInfos().get(0)).getUnit());
			assertEquals("MBUS", ((ChannelInfo)xmlHandler.getProfileData()[1].getChannelInfos().get(0)).getName());
			assertEquals(newDate(17, 4, 2008, 9, 45, 0), xmlHandler.getProfileData()[0].getIntervalData(0).getEndTime());
			assertEquals(firstNumber, ((IntervalValue)xmlHandler.getProfileData()[0].getIntervalData(0).getIntervalValues().get(0)).getNumber().doubleValue());
			assertEquals(newDate(22, 4, 2008, 15, 45, 0), xmlHandler.getProfileData()[0].getIntervalData(504).getEndTime());
			assertEquals(someNumber, ((IntervalValue)xmlHandler.getProfileData()[0].getIntervalData(504).getIntervalValues().get(1)).getNumber().doubleValue());
			assertEquals(newDate(29, 4, 2008, 5, 30, 0), xmlHandler.getProfileData()[0].getIntervalData(xmlHandler.getProfileData()[0].
					getIntervalDatas().size()-1).getEndTime());
			assertEquals(lastNumber, ((IntervalValue)xmlHandler.getProfileData()[0].getIntervalData(xmlHandler.getProfileData()[0].
					getIntervalDatas().size()-1).getIntervalValues().get(0)).getNumber().doubleValue());
			
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
		} catch (SQLException e) {
			fail();
			e.printStackTrace();
		}
	}
	
	private Date newDate(int day, int month, int year, int hour, int min, int sec){
		Calendar cal = Calendar.getInstance(TimeZone.getDefault());
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.MONTH, month-1);
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, min);
		cal.set(Calendar.SECOND, sec);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
}
