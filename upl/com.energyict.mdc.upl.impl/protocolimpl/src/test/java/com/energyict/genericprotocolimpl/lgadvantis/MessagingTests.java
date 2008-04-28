package com.energyict.genericprotocolimpl.lgadvantis;

import com.energyict.cbo.BusinessException;
import com.energyict.edf.messages.MessageReadRegister;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.MeteringWarehouseFactory;
import com.energyict.mdw.core.Rtu;
import com.energyict.mdw.shadow.RtuMessageShadow;
import com.energyict.obis.ObisCode;

import java.sql.SQLException;
import java.util.Date;

import junit.framework.TestCase;

public class MessagingTests extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
		MeteringWarehouse mw = (new MeteringWarehouseFactory()).getBatch(false);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

//	public void testResetAndRediscover(){
//	MessageDiscoverMeters mc = new MessageDiscoverMeters(1);
//
//	RtuMessageShadow shadow = new RtuMessageShadow();
//	shadow.setReleaseDate(new Date());
//	shadow.setContents(mc.xmlEncode());
//	shadow.setTrackingId("ResetAndRediscover");
//	shadow.setRtuId(542);
//
//	try {
//	MeteringWarehouse.getCurrent().getRtuMessageFactory().create(shadow);
//	} catch (SQLException e) {
//	e.printStackTrace();
//	fail();
//	} catch (BusinessException e) {
//	e.printStackTrace();
//	fail();
//	}
//	}


//	public void testIndustrialMeterWriteLow(){
//	MessageWriteRegister mc = new MessageWriteRegister("1.1.1.1.1.1", "10W");

//	RtuMessageShadow shadow = new RtuMessageShadow();
//	shadow.setReleaseDate(new Date());
//	shadow.setContents(mc.xmlEncode());
//	shadow.setTrackingId("IndustrialMeter Disconnect");
//	shadow.setRtuId(64);

//	try {
//	MeteringWarehouse.getCurrent().getRtuMessageFactory().create(shadow);
//	} catch (SQLException e) {
//	e.printStackTrace();
//	fail();
//	} catch (BusinessException e) {
//	e.printStackTrace();
//	fail();
//	}

//	}

//	public void testIndustrialMeterWriteHigh(){
//	MessageWriteRegister mc = new MessageWriteRegister("1.1.1.1.1.1", "60kW");

//	RtuMessageShadow shadow = new RtuMessageShadow();
//	shadow.setReleaseDate(new Date());
//	shadow.setContents(mc.xmlEncode());
//	shadow.setTrackingId("IndustrialMeter Connect");
//	shadow.setRtuId(64);

//	try {
//	MeteringWarehouse.getCurrent().getRtuMessageFactory().create(shadow);
//	} catch (SQLException e) {
//	e.printStackTrace();
//	fail();
//	} catch (BusinessException e) {
//	e.printStackTrace();
//	fail();
//	}
//	}

//	public void testIndustrialMeterRead(){
//	MessageReadIndexes mc = new MessageReadIndexes();

//	RtuMessageShadow shadow = new RtuMessageShadow();
//	shadow.setReleaseDate(new Date());
//	shadow.setContents(mc.xmlEncode());
//	shadow.setTrackingId("IndustrialMeter Read");
//	shadow.setRtuId(64);

//	try {
//	MeteringWarehouse.getCurrent().getRtuMessageFactory().create(shadow);
//	} catch (SQLException e) {
//	e.printStackTrace();
//	fail();
//	} catch (BusinessException e) {
//	e.printStackTrace();
//	fail();
//	}
//	}

//	public void testPerformanceTestUpload(){
//	MessagePostXmlFile mc = new MessagePostXmlFile("C:/cap/performance/T0_R_PV_150 X 8_without_start.xml");

//	RtuMessageShadow shadow = new RtuMessageShadow();
//	shadow.setReleaseDate(new Date());
//	shadow.setContents(mc.xmlEncode());
//	shadow.setTrackingId("Performance upload");
//	shadow.setRtuId(172);

//	try {
//	MeteringWarehouse.getCurrent().getRtuMessageFactory().create(shadow);
//	} catch (SQLException e) {
//	e.printStackTrace();
//	fail();
//	} catch (BusinessException e) {
//	e.printStackTrace();
//	fail();
//	}
//	}

//	public void testPerformanceTestStart(){
//	MessagePerformanceTest mc = new MessagePerformanceTest(0);

//	RtuMessageShadow shadow = new RtuMessageShadow();
//	shadow.setReleaseDate(new Date());
//	shadow.setContents(mc.xmlEncode());
//	shadow.setTrackingId("Performance start");
//	shadow.setRtuId(172);

//	try {
//	MeteringWarehouse.getCurrent().getRtuMessageFactory().create(shadow);
//	} catch (SQLException e) {
//	e.printStackTrace();
//	fail();
//	} catch (BusinessException e) {
//	e.printStackTrace();
//	fail();
//	}
//	}

	public void testReadRegister(){
	Date now = new Date();
	ObisCode code = new ObisCode(0,0,13,0,0,255);
	MessageReadRegister mc = new MessageReadRegister(code.toString());
	RtuMessageShadow shadow = new RtuMessageShadow();
	shadow.setReleaseDate(new Date());
	shadow.setContents(mc.xmlEncode());
	shadow.setTrackingId("Performance start");
	shadow.setRtuId(586);
	try {
	MeteringWarehouse.getCurrent().getRtuMessageFactory().create(shadow);
	Rtu concentrator = MeteringWarehouse.getCurrent().getRtuFactory().find(125);
	CommunicationScheduler scheduler = (CommunicationScheduler) concentrator.getCommunicationSchedulers().iterator().next();
	scheduler.startReadingNow();
	Thread.sleep(20000);
	Rtu meter = MeteringWarehouse.getCurrent().getRtuFactory().find(163);
	RtuRegister register = meter.getRegister(code);
	assertNotNull(register.getReadingAfterOrEqual(now));
	} catch (SQLException e) {
	e.printStackTrace();
	fail();
	} catch (BusinessException e) {
	e.printStackTrace();
	fail();
	} catch (Exception e){
	e.printStackTrace();
	fail();			
	}
	}

//	public void testReadWhatever(){
//	MessageReadLogBook mc = new MessageReadLogBook();
//	RtuMessageShadow shadow = new RtuMessageShadow();
//	shadow.setReleaseDate(new Date());
//	shadow.setContents(mc.xmlEncode());
//	shadow.setTrackingId("IndustrialMeter Connect");
//	shadow.setRtuId(163);
//	try {
//	MeteringWarehouse.getCurrent().getRtuMessageFactory().create(shadow);
//	} catch (SQLException e) {
//	e.printStackTrace();
//	fail();
//	} catch (BusinessException e) {
//	e.printStackTrace();
//	fail();
//	}
//	}

//	public void testWriteWhatever(){
//		MessageWriteRegister mc = new MessageWriteRegister("0.0.128.30.22.255","0");
//
//		RtuMessageShadow shadow = new RtuMessageShadow();
//		shadow.setReleaseDate(new Date());
//		shadow.setContents(mc.xmlEncode());
//		shadow.setTrackingId("IndustrialMeter Connect");
//		shadow.setRtuId(163);
//
//		try {
//			MeteringWarehouse.getCurrent().getRtuMessageFactory().create(shadow);
//		} catch (SQLException e) {
//			e.printStackTrace();
//			fail();
//		} catch (BusinessException e) {
//			e.printStackTrace();
//			fail();
//		}
//
//	}
}
