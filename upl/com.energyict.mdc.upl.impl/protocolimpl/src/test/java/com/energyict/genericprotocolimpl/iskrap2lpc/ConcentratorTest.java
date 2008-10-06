package com.energyict.genericprotocolimpl.iskrap2lpc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.text.ParseException;
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

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.TimeDuration;
import com.energyict.cbo.Unit;
import com.energyict.cpo.Environment;
import com.energyict.interval.RawIntervalRecord;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.CommunicationProtocol;
import com.energyict.mdw.core.IntervalDataStorer;
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
	private String jcnMbusMeter = "com.energyict.genericprotocolimpl.iskrap2lpc.MbusDevice";
	private String testMeter = "TestMeter";
	
	private String august01 = "2008-08-01T00:00:00 +0200";
	private String august28 = "2008-08-28T00:00:00 +0200";
	private String august29 = "2008-08-29T00:00:00 +0200";
	private String august31 = "2008-08-31T00:00:00 +0200";
	private String september01 = "2008-09-01T00:00:00 +0200";
	private String september02 = "2008-09-02T00:00:00 +0200";
	private String september05 = "2008-09-05T00:00:00 +0200";
	private String september06 = "2008-09-06T00:00:00 +0200";
	private String september21 = "2008-09-21T00:00:00 +0200";
	private String september22 = "2008-09-22T00:00:00 +0200";
	private String september23 = "2008-09-23T00:00:00 +0200";
	private String september24 = "2008-09-24T00:00:00 +0200";
	private String october01 = "2008-10-01T00:00:00 +0200";
	
	private CommunicationProtocol commProtMeter = null;
	private Concentrator iskraConcentrator;
	private MeterReadTransaction meterReadTransaction;
	private Rtu meter;
	private Rtu mbusRtu[] = {null, null, null, null, null};
	private RtuType rtuTypeMeter = null;
	
	private List result = new ArrayList();
	
	@BeforeClass
	public static void setUpOnce() {
		Utilities.createEnvironment();
		MeteringWarehouse.createBatchContext(false);
		logger = Logger.getLogger("global");
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
		
		// find out if there is an rtuType defined with this testName, if not, create it
		result = Utilities.mw().getRtuTypeFactory().findByName(testMeter);
		if(result.size() == 0)
			rtuTypeMeter = Utilities.createRtuType(commProtMeter, testMeter, 4);
		else
			rtuTypeMeter = (RtuType)result.get(0);
		
	}

	@After
	public void tearDown() throws Exception {
		// first delete the device
		List result = Utilities.mw().getRtuFactory().findByName("12345678");
		if (result.size() > 0)
			for(int i = 0; i < result.size(); i++)
				((Rtu)result.get(i)).delete();
		
		// then the deviceType
		result = Utilities.mw().getRtuTypeFactory().findByName(testMeter);
		if (result.size() > 0)
			for(int i = 0; i < result.size(); i++){
				List refMeters = Utilities.mw().getRtuFactory().findByType((RtuType)result.get(i));
				for(int k = 0; k < refMeters.size(); k++){
					((Rtu)result.get(k)).delete();	
				}
				((RtuTypeImpl)result.get(i)).delete();
			}
		
		// then the communication profile
		result = Utilities.mw().getCommunicationProtocolFactory().findByName(jcnIskraMeter);
		if (result.size() > 0)
			for(int i = 0; i < result.size(); i++)
				((CommunicationProtocolImpl)result.get(i)).delete();
	}

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
			
			meter = Utilities.addPropertyToRtu(meter, Constant.CHANNEL_MAP, "1.8.0+9:2.8.0+9");
			iskraConcentrator.setLogger(logger);
			
			meterReadTransaction.setMeter(meter);
			XmlHandler xmlHandler = new XmlHandler(logger, meterReadTransaction.getChannelMap());
			xmlHandler.setChannelUnit(Unit.get(BaseUnit.WATTHOUR, 3));
			meterReadTransaction.setProfileTestName(Constant.profileFiles1);
			meterReadTransaction.importProfile(meter, xmlHandler, false);
			
			assertEquals(Unit.get("kWh"), ((ChannelInfo)xmlHandler.getProfileData().getChannelInfos().get(0)).getUnit());
			assertEquals("channel 0  1.0.1.8.0.255", ((ChannelInfo)xmlHandler.getProfileData().getChannelInfos().get(0)).getName());
			assertEquals(newDate(17, 4, 2008, 9, 45, 0), xmlHandler.getProfileData().getIntervalData(0).getEndTime());
			assertEquals(firstNumber, ((IntervalValue)xmlHandler.getProfileData().getIntervalData(0).getIntervalValues().get(0)).getNumber().doubleValue());
			assertEquals(newDate(22, 4, 2008, 15, 45, 0), xmlHandler.getProfileData().getIntervalData(504).getEndTime());
			assertEquals(someNumber, ((IntervalValue)xmlHandler.getProfileData().getIntervalData(504).getIntervalValues().get(1)).getNumber().doubleValue());
			assertEquals(newDate(29, 4, 2008, 5, 30, 0), xmlHandler.getProfileData().getIntervalData(xmlHandler.getProfileData().
					getIntervalDatas().size()-1).getEndTime());
			assertEquals(lastNumber, ((IntervalValue)xmlHandler.getProfileData().getIntervalData(xmlHandler.getProfileData().
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

	@Test
	public void importDailyMonthlyTest(){
		try {
			
			String property = "0:0:0:0:1.8.1+9d:1.8.1+9m";
			result = Utilities.mw().getRtuFactory().findByName(testMeter);
			if(result.size() == 0)
				meter = Utilities.createRtu(rtuTypeMeter, "12345678", 900);
			else
				meter = (Rtu)result.get(0);
			meter = Utilities.addChannel(meter, TimeDuration.DAYS, 5);
			meter = Utilities.addChannel(meter, TimeDuration.MONTHS, 6);
			
			if(!Utilities.getChannelWithProfileIndex(meter, 5).getInterval().equals(new TimeDuration(1, TimeDuration.DAYS)))
				fail();
			if(!Utilities.getChannelWithProfileIndex(meter, 6).getInterval().equals(new TimeDuration(1, TimeDuration.MONTHS)))
				fail();
			
			meter = Utilities.addPropertyToRtu(meter, "ChannelMap", property);
			iskraConcentrator.setLogger(logger);
			meterReadTransaction.setMeter(meter);
			XmlHandler xmlHandler = new XmlHandler(logger, meterReadTransaction.getChannelMap());
			meterReadTransaction.setBillingDaily(Constant.billingDaily);
			meterReadTransaction.setBillingMonthly(Constant.billingMonthly);
			xmlHandler.setDailyMonthlyProfile(true);
			meterReadTransaction.importDailyMonthly(meter, xmlHandler, meter.getSerialNumber());
			 // do NOT store it, we already do this in the importDailyMonthly method
			xmlHandler.setDailyMonthlyProfile(false);
			Environment.getDefault().execute(meterReadTransaction.getStoreObjects());
			
			
			// start the testing
			Date sep02 = Constant.getInstance().getDateFormat().parse(september02);
			RawIntervalRecord raw0902 = (RawIntervalRecord)meter.getChannel(4).getIntervalData(subDay(sep02), addDay(sep02)).get(0);
			assertEquals(new BigDecimal(new BigInteger("20246"), 3), raw0902.getRawValue());
			assertEquals(sep02, raw0902.getDate());
			
			Date aug31 = Constant.getInstance().getDateFormat().parse(august31);
			RawIntervalRecord raw0831 = (RawIntervalRecord)meter.getChannel(4).getIntervalData(subDay(aug31), addDay(aug31)).get(0);
			assertEquals(new BigDecimal(new BigInteger("19834"), 3), raw0831.getRawValue());
			assertEquals(aug31, raw0831.getDate());
			
			Date aug29 = Constant.getInstance().getDateFormat().parse(august29);
			RawIntervalRecord raw0829 = (RawIntervalRecord)meter.getChannel(4).getIntervalData(subDay(aug29), addDay(aug29)).get(0);
			assertEquals(new BigDecimal(new BigInteger("19421"), 3), raw0829.getRawValue());
			assertEquals(aug29, raw0829.getDate());
			
			Date aug28 = Constant.getInstance().getDateFormat().parse(august28);
			RawIntervalRecord raw0828 = (RawIntervalRecord)meter.getChannel(4).getIntervalData(subDay(aug28), addDay(aug28)).get(0);
			assertEquals(new BigDecimal(new BigInteger("19385"), 3), raw0828.getRawValue());
			assertEquals(aug28, raw0828.getDate());
			
			Date aug01 = Constant.getInstance().getDateFormat().parse(august01);
			RawIntervalRecord raw0801 = (RawIntervalRecord)meter.getChannel(5).getIntervalData(subDay(aug01), addDay(aug01)).get(0);
			assertEquals(new BigDecimal(new BigInteger("19385"), 3), raw0801.getRawValue());
			assertEquals(aug01, raw0801.getDate());
			
			Date sep01 = Constant.getInstance().getDateFormat().parse(september01);
			RawIntervalRecord raw0901 = (RawIntervalRecord)meter.getChannel(5).getIntervalData(subDay(sep01), addDay(sep01)).get(0);
			assertEquals(new BigDecimal(new BigInteger("20125"), 3), raw0901.getRawValue());
			assertEquals(sep01, raw0901.getDate());
					
			
		} catch (SQLException e) {
			e.printStackTrace();
			fail();
		} catch (BusinessException e) {
			e.printStackTrace();
			fail();
		} catch (InvalidPropertyException e) {
			e.printStackTrace();
			fail();
		} catch (ServiceException e) {
			e.printStackTrace();
			fail();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} catch (ParseException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void importTwoDailyMonthlyProfilesTest(){
		try {
			String property = "0:0:0:0:1.8.1+9d:1.8.1+9m";
			result = Utilities.mw().getRtuFactory().findByName(testMeter);
			if(result.size() == 0)
				meter = Utilities.createRtu(rtuTypeMeter, "12345678", 900);
			else
				meter = (Rtu)result.get(0);
			meter = Utilities.addChannel(meter, TimeDuration.DAYS, 5);
			meter = Utilities.addChannel(meter, TimeDuration.MONTHS, 6);
			
			meter = Utilities.addPropertyToRtu(meter, "ChannelMap", property);
			iskraConcentrator.setLogger(logger);
			meterReadTransaction.setMeter(meter);
			
			XmlHandler xmlHandler = new XmlHandler(logger, meterReadTransaction.getChannelMap());
			xmlHandler.setDailyMonthlyProfile(true);
			meterReadTransaction.setBillingDaily(Constant.dailyto0509);
			meterReadTransaction.setBillingMonthly(Constant.monthlyto0509);
			meterReadTransaction.importDailyMonthly(meter, xmlHandler, meter.getSerialNumber());
			Environment.getDefault().execute(meterReadTransaction.getStoreObjects());

			Date sep05 = Constant.getInstance().getDateFormat().parse(september05);
			RawIntervalRecord raw0905 = (RawIntervalRecord)meter.getChannel(4).getIntervalData(subDay(sep05), addDay(sep05)).get(0);
			assertEquals(new BigDecimal(new BigInteger("20612"), 3), raw0905.getRawValue());
			assertEquals(new BigDecimal(new BigInteger("122"), 3),raw0905.getValue());
			assertEquals(sep05, raw0905.getDate());
			
			xmlHandler = new XmlHandler(logger, meterReadTransaction.getChannelMap());
			xmlHandler.setDailyMonthlyProfile(true);
			meterReadTransaction.setBillingDaily(Constant.dailyfrom0509);
			meterReadTransaction.setBillingMonthly(Constant.monthlyfrom0509);
			meterReadTransaction.importDailyMonthly(meter, xmlHandler, meter.getSerialNumber());
			Environment.getDefault().execute(meterReadTransaction.getStoreObjects());
			
			sep05 = Constant.getInstance().getDateFormat().parse(september05);
			raw0905 = (RawIntervalRecord)meter.getChannel(4).getIntervalData(subDay(sep05), addDay(sep05)).get(0);
			assertEquals(new BigDecimal(new BigInteger("20612"), 3), raw0905.getRawValue());
			assertEquals(new BigDecimal(new BigInteger("122"), 3),raw0905.getValue());
			assertEquals(sep05, raw0905.getDate());
			
			Date sep06 = Constant.getInstance().getDateFormat().parse(september06);
			RawIntervalRecord raw0906 = (RawIntervalRecord)meter.getChannel(4).getIntervalData(subDay(sep06), addDay(sep06)).get(0);
			assertEquals(new BigDecimal(new BigInteger("20734"), 3), raw0906.getRawValue());
			assertEquals(new BigDecimal(new BigInteger("122"), 3),raw0906.getValue());
			assertEquals(sep06, raw0906.getDate());

			xmlHandler.setDailyMonthlyProfile(false);
		} catch (SQLException e) {
			e.printStackTrace();
			fail();
		} catch (BusinessException e) {
			e.printStackTrace();
			fail();
		} catch (InvalidPropertyException e) {
			e.printStackTrace();
			fail();
		} catch (ServiceException e) {
			e.printStackTrace();
			fail();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} catch (ParseException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void importDailyMonthlyFromPLR(){
		try {
			String property = "0:0:0:0:1.8.1+9d:1.8.2m";
			result = Utilities.mw().getRtuFactory().findByName(testMeter);
			if(result.size() == 0)
				meter = Utilities.createRtu(rtuTypeMeter, "12345678", 900);
			else
				meter = (Rtu)result.get(0);
			meter = Utilities.addChannel(meter, TimeDuration.DAYS, 5);
			meter = Utilities.addChannel(meter, TimeDuration.MONTHS, 6);
			
			meter = Utilities.addPropertyToRtu(meter, "ChannelMap", property);
			iskraConcentrator.setLogger(logger);
			meterReadTransaction.setMeter(meter);
			
			XmlHandler xmlHandler = new XmlHandler(logger, meterReadTransaction.getChannelMap());
			xmlHandler.setDailyMonthlyProfile(true);
			meterReadTransaction.setBillingDaily(Constant.dailyResult);
			meterReadTransaction.setBillingMonthly(Constant.monthlyResult);
			meterReadTransaction.importDailyMonthly(meter, xmlHandler, meter.getSerialNumber());
			Environment.getDefault().execute(meterReadTransaction.getStoreObjects());
			
			// checks if the overhead intervals are deleted
			List intervalCount;
			intervalCount = meter.getChannel(4).getIntervalData(subDay(Constant.getInstance().getDateFormat().parse(september01)), addDay(Constant.getInstance().getDateFormat().parse(september24)));
			assertEquals(3, intervalCount.size());
			intervalCount = meter.getChannel(5).getIntervalData(subDay(Constant.getInstance().getDateFormat().parse(august01)), Constant.getInstance().getDateFormat().parse(september24));
			assertEquals(2, intervalCount.size());
			
			Date sep21 = Constant.getInstance().getDateFormat().parse(september21);
			RawIntervalRecord raw0921 = (RawIntervalRecord)meter.getChannel(4).getIntervalData(subDay(sep21), addDay(sep21)).get(0);
			assertEquals(new BigDecimal(new BigInteger("20123"), 3), raw0921.getRawValue());
			assertEquals(sep21, raw0921.getDate());

			Date sep22 = Constant.getInstance().getDateFormat().parse(september22);
			RawIntervalRecord raw0922 = (RawIntervalRecord)meter.getChannel(4).getIntervalData(subDay(sep22), addDay(sep22)).get(0);
			assertEquals(new BigDecimal(new BigInteger("21123"), 3), raw0922.getRawValue());
			assertEquals(sep22, raw0922.getDate());
			
			Date sep24 = Constant.getInstance().getDateFormat().parse(september24);
			RawIntervalRecord raw0924 = (RawIntervalRecord)meter.getChannel(4).getIntervalData(subDay(sep24), addDay(sep24)).get(0);
			assertEquals(new BigDecimal(new BigInteger("22123"), 3), raw0924.getRawValue());
			assertEquals(sep24, raw0924.getDate());
			
			Date aug01 = Constant.getInstance().getDateFormat().parse(august01);
			RawIntervalRecord raw0801 = (RawIntervalRecord)meter.getChannel(5).getIntervalData(subDay(aug01), addDay(aug01)).get(0);
			assertEquals(new BigDecimal(new BigInteger("5")), raw0801.getRawValue());
			assertEquals(aug01, raw0801.getDate());
			
			Date sep01 = Constant.getInstance().getDateFormat().parse(september01);
			RawIntervalRecord raw0901 = (RawIntervalRecord)meter.getChannel(5).getIntervalData(subDay(sep01), addDay(sep01)).get(0);
			assertEquals(new BigDecimal(new BigInteger("10")), raw0901.getRawValue());
			assertEquals(sep01, raw0901.getDate());
			
		} catch (SQLException e) {
			e.printStackTrace();
			fail();
		} catch (BusinessException e) {
			e.printStackTrace();
			fail();
		} catch (InvalidPropertyException e) {
			e.printStackTrace();
			fail();
		} catch (ServiceException e) {
			e.printStackTrace();
			fail();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} catch (ParseException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void singelMonthlyValueTest(){
		try {
			String property = "0:0:0:0:1.8.1+9m";
			result = Utilities.mw().getRtuFactory().findByName(testMeter);
			if(result.size() == 0)
				meter = Utilities.createRtu(rtuTypeMeter, "12345678", 900);
			else
				meter = (Rtu)result.get(0);
			meter = Utilities.addChannel(meter, TimeDuration.MONTHS, 5);
			
			meter = Utilities.addPropertyToRtu(meter, "ChannelMap", property);
			iskraConcentrator.setLogger(logger);
			meterReadTransaction.setMeter(meter);
			
			XmlHandler xmlHandler = new XmlHandler(logger, meterReadTransaction.getChannelMap());
			xmlHandler.setDailyMonthlyProfile(true);
			meterReadTransaction.setBillingMonthly(Constant.oneMonthlyValue);
			meterReadTransaction.importDailyMonthly(meter, xmlHandler, meter.getSerialNumber());
			Environment.getDefault().execute(meterReadTransaction.getStoreObjects());
			
			Date oct01 = Constant.getInstance().getDateFormat().parse(october01);
			RawIntervalRecord raw1001 = (RawIntervalRecord)meter.getChannel(4).getIntervalData(subDay(oct01), addDay(oct01)).get(0);
			assertEquals(new BigDecimal(new BigInteger("25263"), 3), raw1001.getRawValue());
			assertEquals(oct01, raw1001.getDate());
			
		} catch (SQLException e) {
			e.printStackTrace();
			fail();
		} catch (BusinessException e) {
			e.printStackTrace();
			fail();
		} catch (InvalidPropertyException e) {
			e.printStackTrace();
			fail();
		} catch (ServiceException e) {
			e.printStackTrace();
			fail();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} catch (ParseException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void mutlipleSingleStores(){
		try {
			String property = "0:0:0:0:1.8.1+9m";
			result = Utilities.mw().getRtuFactory().findByName(testMeter);
			if(result.size() == 0)
				meter = Utilities.createRtu(rtuTypeMeter, "12345678", 900);
			else
				meter = (Rtu)result.get(0);
			meter = Utilities.addChannel(meter, TimeDuration.MONTHS, 5);
			
			meter = Utilities.addPropertyToRtu(meter, "ChannelMap", property);
			iskraConcentrator.setLogger(logger);
			meterReadTransaction.setMeter(meter);
			
			XmlHandler xmlHandler = new XmlHandler(logger, meterReadTransaction.getChannelMap());
			xmlHandler.setDailyMonthlyProfile(true);
			meterReadTransaction.setBillingMonthly(Constant.billingMonthly);
			meterReadTransaction.importDailyMonthly(meter, xmlHandler, meter.getSerialNumber());
			Environment.getDefault().execute(meterReadTransaction.getStoreObjects());
			
			xmlHandler = new XmlHandler(logger, meterReadTransaction.getChannelMap());
			xmlHandler.setDailyMonthlyProfile(true);
			meterReadTransaction.setBillingMonthly(Constant.oneMonthlyValue);
			meterReadTransaction.importDailyMonthly(meter, xmlHandler, meter.getSerialNumber());
			Environment.getDefault().execute(meterReadTransaction.getStoreObjects());
			
			Date oct01 = Constant.getInstance().getDateFormat().parse(october01);
			Date aug01 = Constant.getInstance().getDateFormat().parse(august01);
			RawIntervalRecord raw1001 = (RawIntervalRecord)meter.getChannel(4).getIntervalData(subDay(oct01), addDay(oct01)).get(0);
			assertEquals(new BigDecimal(new BigInteger("25263"), 3), raw1001.getRawValue());
			assertEquals(oct01, raw1001.getDate());
			
		} catch (SQLException e) {
			e.printStackTrace();
			fail();
		} catch (BusinessException e) {
			e.printStackTrace();
			fail();
		} catch (InvalidPropertyException e) {
			e.printStackTrace();
			fail();
		} catch (ServiceException e) {
			e.printStackTrace();
			fail();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} catch (ParseException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	private void mbusAfter() throws BusinessException, SQLException{
		// first delete all the mbusMeters
		List result = Utilities.mw().getRtuFactory().findByType((RtuType)Utilities.mw().getRtuTypeFactory().findByName("mbusMeter").get(0));
		if (result.size() > 0){
			for(int j = 0; j < result.size(); j++)
				((Rtu)result.get(j)).delete();
		}
		
		// then the deviceType
		result = Utilities.mw().getRtuTypeFactory().findByName("mbusMeter");
		if (result.size() > 0)
			for(int i = 0; i < result.size(); i++)
				((RtuTypeImpl)result.get(0)).delete();
		
		// then the communication profile
		result = Utilities.mw().getCommunicationProtocolFactory().findByName(jcnMbusMeter);
		if (result.size() > 0)
			for(int i = 0; i < result.size(); i++)
				((CommunicationProtocolImpl)result.get(0)).delete();
	}
	
	@Test
	public void mbusTest() throws BusinessException, SQLException{
		try{
			
			String property = "0:0:0:0:1.8.1+9m";
			result = Utilities.mw().getRtuFactory().findByName(testMeter);
			if(result.size() == 0)
				meter = Utilities.createRtu(rtuTypeMeter, "12345678", 900);
			else
				meter = (Rtu)result.get(0);
			meter = Utilities.addChannel(meter, TimeDuration.MONTHS, 5);
			
			meter = Utilities.addPropertyToRtu(meter, "ChannelMap", property);
			iskraConcentrator.setLogger(logger);
			meterReadTransaction.setMeter(meter);
			
			/** MBus installation part */
			// find out if the communication profile exists, if not, create it
			result = Utilities.mw().getCommunicationProtocolFactory().findAll();
			commProtMeter = null;
			for(int i = 0; i < result.size(); i++){
				if(((CommunicationProtocol)result.get(i)).getJavaClassName().equalsIgnoreCase(jcnMbusMeter)){
					commProtMeter = (CommunicationProtocol)result.get(i);
					break;
				}
			}
			if(commProtMeter == null)
				commProtMeter = Utilities.createCommunicationProtocol(jcnMbusMeter);
			
			// find out if there is an rtuType defined with this testName, if not, create it
			result = Utilities.mw().getRtuTypeFactory().findByName("mbusMeter");
			if(result.size() == 0)
				rtuTypeMeter = Utilities.createRtuType(commProtMeter, "mbusMeter", 4);
			else
				rtuTypeMeter = (RtuType)result.get(0);
			
			for(int i = 0; i < 4; i++){
				result = Utilities.mw().getRtuFactory().findByName("mbusMeter"+i);
				if(result.size() == 0)
					mbusRtu[i] = Utilities.createRtu(rtuTypeMeter, ""+i+i+i+i, 3600);
				else 
					mbusRtu[i] = (Rtu)result.get(0);
				mbusRtu[i] = Utilities.addChannel(mbusRtu[i], TimeDuration.DAYS, 5);
				mbusRtu[i] = Utilities.addPropertyToRtu(mbusRtu[i], "ChannelMap", "1:1:1:1:0.1.128.50.0d");
				mbusRtu[i].updateGateway(meter);
			}
			
			meterReadTransaction.checkMbusDevices();
			
			for(int i = 0; i < 4; i++){
				MbusDevice mbd = meterReadTransaction.mbusDevices[i];
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			fail();
		} catch (BusinessException e) {
			e.printStackTrace();
			fail();
		} catch (NumberFormatException e) {
			e.printStackTrace();
			fail();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} catch (ServiceException e) {
			e.printStackTrace();
			fail();
		} finally {
			mbusAfter();
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
	
	private Date addDay(Date time){
		Calendar cal = Calendar.getInstance();
		cal.setTime(time);
		cal.add(Calendar.DAY_OF_MONTH, 1);
		return cal.getTime();
	}
	
	private Date subDay(Date time){
		Calendar cal = Calendar.getInstance();
		cal.setTime(time);
		cal.add(Calendar.DAY_OF_MONTH, -1);
		return cal.getTime();
	}
}
