/**
 * 
 */
package com.energyict.protocolimpl.edf.trimaran2p.core;


import com.energyict.cbo.Utils;
import com.energyict.protocol.IntervalData;
import com.energyict.protocolimpl.edf.trimaran2p.Trimaran2P;
import com.energyict.protocolimpl.edf.trimarandlms.axdr.TrimaranOctetString;
import org.junit.*;

import java.io.*;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author gna
 *
 */
public class CourbeChargeTest {
	
	private Trimaran2P deuxP;
	private TrimaranOctetString interval160 = new TrimaranOctetString(new byte[]{(byte)0x07, (byte)0xD8, (byte)0x06, (byte)0x01, (byte)0xFF, (byte)0x02, (byte)0x32, (byte)0x00});
	private TrimaranOctetString interval304 = new TrimaranOctetString(new byte[]{(byte)0x07, (byte)0xD8, (byte)0x06, (byte)0x02, (byte)0xFF, (byte)0x02, (byte)0x32, (byte)0x00});
	private TrimaranOctetString interval448 = new TrimaranOctetString(new byte[]{(byte)0x07, (byte)0xD8, (byte)0x06, (byte)0x03, (byte)0xFF, (byte)0x02, (byte)0x32, (byte)0x00});
	private TrimaranOctetString interval592 = new TrimaranOctetString(new byte[]{(byte)0x07, (byte)0xD8, (byte)0x06, (byte)0x04, (byte)0xFF, (byte)0x02, (byte)0x32, (byte)0x00});
	private TrimaranOctetString interval736 = new TrimaranOctetString(new byte[]{(byte)0x07, (byte)0xD8, (byte)0x06, (byte)0x05, (byte)0xFF, (byte)0x02, (byte)0x32, (byte)0x00});
	private TrimaranOctetString interval767 = new TrimaranOctetString(new byte[]{(byte)0x07, (byte)0xD8, (byte)0x06, (byte)0x05, (byte)0xFF, (byte)0x08, (byte)0x00, (byte)0x00});
	
	private TrimaranOctetString interval704 = new TrimaranOctetString(new byte[]{(byte)0x07, (byte)0xD8, (byte)0x06, (byte)0x19, (byte)0xFF, (byte)0x09, (byte)0x1E, (byte)0x00});
	private TrimaranOctetString interval706 = new TrimaranOctetString(new byte[]{(byte)0x07, (byte)0xD8, (byte)0x06, (byte)0x19, (byte)0xFF, (byte)0x09, (byte)0x32, (byte)0x00});
	private TrimaranOctetString interval707 = new TrimaranOctetString(new byte[]{(byte)0x07, (byte)0xD8, (byte)0x06, (byte)0x19, (byte)0xFF, (byte)0x0A, (byte)0x00, (byte)0x00});
	private TrimaranOctetString interval733 = new TrimaranOctetString(new byte[]{(byte)0x07, (byte)0xD8, (byte)0x06, (byte)0x19, (byte)0xFF, (byte)0x0E, (byte)0x14, (byte)0x00});
	private TrimaranOctetString interval751 = new TrimaranOctetString(new byte[]{(byte)0x07, (byte)0xD8, (byte)0x06, (byte)0x19, (byte)0xFF, (byte)0x11, (byte)0x14, (byte)0x00});

	private TrimaranOctetString interval540 = new TrimaranOctetString(new byte[]{(byte)0x07, (byte)0xD8, (byte)0x06, (byte)0x19, (byte)0xFF, (byte)0x09, (byte)0x28, (byte)0x00});
	private TrimaranOctetString interval542 = new TrimaranOctetString(new byte[]{(byte)0x07, (byte)0xD8, (byte)0x06, (byte)0x19, (byte)0xFF, (byte)0x0A, (byte)0x00, (byte)0x00});
	private TrimaranOctetString interval555 = new TrimaranOctetString(new byte[]{(byte)0x07, (byte)0xD8, (byte)0x06, (byte)0x19, (byte)0xFF, (byte)0x0C, (byte)0x0A, (byte)0x00});
	private TrimaranOctetString interval568 = new TrimaranOctetString(new byte[]{(byte)0x07, (byte)0xD8, (byte)0x06, (byte)0x19, (byte)0xFF, (byte)0x0E, (byte)0x14, (byte)0x00});
	private TrimaranOctetString interval569 = new TrimaranOctetString(new byte[]{(byte)0x07, (byte)0xD8, (byte)0x06, (byte)0x19, (byte)0xFF, (byte)0x0E, (byte)0x1E, (byte)0x00});
	private TrimaranOctetString interval586 = new TrimaranOctetString(new byte[]{(byte)0x07, (byte)0xD8, (byte)0x06, (byte)0x19, (byte)0xFF, (byte)0x11, (byte)0x14, (byte)0x00});
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		deuxP = new Trimaran2P();
		deuxP.init(null, null, TimeZone.getTimeZone("ECT"), null);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		deuxP.release();
	}
	
	@Test
	public void doParseTest(){
		
		CourbeCharge cc = new CourbeCharge(deuxP.getTrimaranObjectFactory());
		FileInputStream fis;
		ObjectInputStream ois;
		File file;
		int[] values;
		Calendar cal = Calendar.getInstance(deuxP.getTimeZone());	//set the last day of the profile to 05/06/08 - 08:07:23
		cal.set(Calendar.DAY_OF_MONTH, 5);
		cal.set(Calendar.MONTH, 5);
		cal.set(Calendar.YEAR, 2008);
		cal.set(Calendar.HOUR_OF_DAY, 8);
		cal.set(Calendar.MINUTE, 7);
		cal.set(Calendar.SECOND, 23);
		
		try {
			
			cc.setNow(cal.getTime());
			cc.getTrimaranObjectFactory().setParameters(new Parameters(cc.getTrimaranObjectFactory()));
			cc.getTrimaranObjectFactory().getParameters().setTCourbeCharge(10);
			cc.getTrimaranObjectFactory().getParameters().setCcReact(false); 	// we must have two channels
			cc.initCollectionsTEC();
			cc.getTrimaranObjectFactory().getTrimaran().setMeterVersion("TEC");
			assertEquals(2, cc.getProfileData().getChannelInfos().size());
			
			cc.getTrimaranObjectFactory().getParameters().setCcReact(true);		// we must have six channels
			cc.initCollectionsTEC();
			assertEquals(6, cc.getProfileData().getChannelInfos().size());
			
			for(int i = 0; i < 6; i++){
				file = new File(Utils.class.getResource("/com/energyict/protocolimpl/edf/trimaran/Object_Values_0406_long" + i + ".bin").getFile());
				fis = new FileInputStream(file);
				ois = new ObjectInputStream(fis);
				values = (int[])ois.readObject();
				cc.addValues(values);
				cc.doParse();
			}
			
			cc.aggregateAndRemoveDuplicates();
			
			IntervalData inter160 = new IntervalData(interval160.toDate(deuxP.getTimeZone()), 0, 0, 0);
			inter160.addValue(new Integer(992)); inter160.addValue(new Integer(0)); inter160.addValue(new Integer(899)); inter160.addValue(new Integer(0)); inter160.addValue(new Integer(0)); inter160.addValue(new Integer(0));
			for(int i = 0; i < inter160.getIntervalValues().size(); i++) {
				assertEquals(cc.getProfileData().getIntervalData(160).get(i), inter160.get(i));
			}
			
			IntervalData inter304 = new IntervalData(interval304.toDate(deuxP.getTimeZone()), 0, 0, 0);
			inter304.addValue(new Integer(1026)); inter304.addValue(new Integer(0)); inter304.addValue(new Integer(938)); inter304.addValue(new Integer(0)); inter304.addValue(new Integer(0)); inter304.addValue(new Integer(0));
			for(int i = 0; i < inter304.getIntervalValues().size(); i++) {
				assertEquals(cc.getProfileData().getIntervalData(304).get(i), inter304.get(i));
			}
			
			IntervalData inter448 = new IntervalData(interval448.toDate(deuxP.getTimeZone()), 0, 0, 0);
			inter448.addValue(new Integer(921)); inter448.addValue(new Integer(0)); inter448.addValue(new Integer(834)); inter448.addValue(new Integer(0)); inter448.addValue(new Integer(0)); inter448.addValue(new Integer(0));
			for(int i = 0; i < inter448.getIntervalValues().size(); i++) {
				assertEquals(cc.getProfileData().getIntervalData(448).get(i), inter448.get(i));
			}
			
			IntervalData inter592 = new IntervalData(interval592.toDate(deuxP.getTimeZone()), 0, 0, 0);
			inter592.addValue(new Integer(222)); inter592.addValue(new Integer(0)); inter592.addValue(new Integer(219)); inter592.addValue(new Integer(0)); inter592.addValue(new Integer(0)); inter592.addValue(new Integer(0));
			for(int i = 0; i < inter592.getIntervalValues().size(); i++) {
				assertEquals(cc.getProfileData().getIntervalData(592).get(i), inter592.get(i));
			}
			
			IntervalData inter736 = new IntervalData(interval736.toDate(deuxP.getTimeZone()), 0, 0, 0);
			inter736.addValue(new Integer(206)); inter736.addValue(new Integer(0)); inter736.addValue(new Integer(199)); inter736.addValue(new Integer(0)); inter736.addValue(new Integer(0)); inter736.addValue(new Integer(0));
			for(int i = 0; i < inter736.getIntervalValues().size(); i++) {
				assertEquals(cc.getProfileData().getIntervalData(736).get(i), inter736.get(i));
			}
			
			IntervalData inter767 = new IntervalData(interval767.toDate(deuxP.getTimeZone()), 0, 0, 0);
			inter767.addValue(new Integer(225)); inter767.addValue(new Integer(0)); inter767.addValue(new Integer(222)); inter767.addValue(new Integer(0)); inter767.addValue(new Integer(0)); inter767.addValue(new Integer(0));
			for(int i = 0; i < inter767.getIntervalValues().size(); i++) {
				assertEquals(cc.getProfileData().getIntervalData(cc.getProfileData().getIntervalDatas().size()-1).get(i), inter767.get(i));
			}
			
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void doParseTestTEP(){
		
		FileInputStream fis;
		ObjectInputStream ois;
		File file;
		int[] values;
		CourbeCharge cc = new CourbeCharge(deuxP.getTrimaranObjectFactory());
		
		// Test the TEP version
		try {
			cc.getTrimaranObjectFactory().getTrimaran().setMeterVersion("TEP");
			cc.getTrimaranObjectFactory().setParameters(new Parameters(cc.getTrimaranObjectFactory()));
			cc.getTrimaranObjectFactory().getParameters().setTCourbeCharge(10);
			cc.getTrimaranObjectFactory().getParameters().setCcReact(false);		// we must have two channels
			cc.initCollectionsTEP();
			cc.setNow(new Date(System.currentTimeMillis()));
			
			for(int i = 1; i < 6; i++){
				file = new File(Utils.class.getResource("/com/energyict/protocolimpl/edf/trimaran/deuxp857/089807000857Profile_" + i + ".bin").getFile());
				fis = new FileInputStream(file);
				ois = new ObjectInputStream(fis);
				values = (int[])ois.readObject();
				cc.addValues(values);
				cc.doParse();
			}
			
			// intervalcount must match
			assertEquals(cc.getProfileData().getNumberOfIntervals(), 799);
			// channels must match
			assertEquals(cc.getProfileData().getNumberOfChannels(), 2);

			IntervalData inter704 = new IntervalData(interval704.toDate(deuxP.getTimeZone()), 4, 0, 0);
			inter704.addValue(new Integer(0)); inter704.addValue(new Integer(0));
			assertEquals(cc.getProfileData().getIntervalData(704).get(0), inter704.get(0));
			assertEquals(cc.getProfileData().getIntervalData(704).get(1), inter704.get(1));
			
			IntervalData inter706 = new IntervalData(interval706.toDate(deuxP.getTimeZone()), 0, 0, 0);
			inter706.addValue(new Integer(684)); inter706.addValue(new Integer(1186));
			assertEquals(cc.getProfileData().getIntervalData(706).get(0), inter706.get(0));
			assertEquals(cc.getProfileData().getIntervalData(706).get(1), inter706.get(1));
			
			IntervalData inter707 = new IntervalData(interval707.toDate(deuxP.getTimeZone()), 4, 0, 0);
			inter707.addValue(new Integer(1090)); inter707.addValue(new Integer(1888));
			assertEquals(cc.getProfileData().getIntervalData(707).get(0), inter707.get(0));
			assertEquals(cc.getProfileData().getIntervalData(707).get(1), inter707.get(1));

			IntervalData inter733 = new IntervalData(interval733.toDate(deuxP.getTimeZone()), 2048, 0, 0);
			inter733.addValue(new Integer(992)); inter733.addValue(new Integer(2033));
			assertEquals(cc.getProfileData().getIntervalData(733).get(0), inter733.get(0));
			assertEquals(cc.getProfileData().getIntervalData(733).get(1), inter733.get(1));
			
			IntervalData inter751 = new IntervalData(interval751.toDate(deuxP.getTimeZone()), 0, 0, 0);
			inter751.addValue(new Integer(311)); inter751.addValue(new Integer(855));
			assertEquals(cc.getProfileData().getIntervalData(751).get(0), inter751.get(0));
			assertEquals(cc.getProfileData().getIntervalData(751).get(1), inter751.get(1));
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			fail();
		}
		
	}
	
	@Test
	public void doParseTestTEC(){
		
		FileInputStream fis;
		ObjectInputStream ois;
		File file;
		int[] values;
		CourbeCharge cc = new CourbeCharge(deuxP.getTrimaranObjectFactory());
		// Test the TEC version
		try {
			cc.getTrimaranObjectFactory().getTrimaran().setMeterVersion("TEC");
			cc.getTrimaranObjectFactory().setParameters(new Parameters(cc.getTrimaranObjectFactory()));
			cc.getTrimaranObjectFactory().getParameters().setTCourbeCharge(10);
			cc.getTrimaranObjectFactory().getParameters().setCcReact(true);			// we must have six channels
			cc.initCollectionsTEC();
			cc.setNow(new Date(System.currentTimeMillis()));
			
			for(int i = 1; i < 6; i++){
				file = new File(Utils.class.getResource("/com/energyict/protocolimpl/edf/trimaran/deuxp201/080307000201Profile_" + i + ".bin").getFile());
				fis = new FileInputStream(file);
				ois = new ObjectInputStream(fis);
				values = (int[])ois.readObject();
				cc.addValues(values);
				cc.doParse();
			}
			
			// intervalcount must match
			assertEquals(cc.getProfileData().getNumberOfIntervals(), 800);
			// channels must match
			assertEquals(cc.getProfileData().getNumberOfChannels(), 6);

			IntervalData inter540 = new IntervalData(interval540.toDate(deuxP.getTimeZone()), 4, 0, 0);
			inter540.addValue(new Integer(0)); inter540.addValue(new Integer(0)); inter540.addValue(new Integer(0));
			inter540.addValue(new Integer(2)); inter540.addValue(new Integer(0)); inter540.addValue(new Integer(4));
			for(int i = 0; i < inter540.getIntervalValues().size(); i++) {
				assertEquals(cc.getProfileData().getIntervalData(540).get(i), inter540.get(i));
			}

			IntervalData inter542 = new IntervalData(interval542.toDate(deuxP.getTimeZone()), 36, 0, 0);
			inter542.addValue(new Integer(0)); inter542.addValue(new Integer(0)); inter542.addValue(new Integer(0));
			inter542.addValue(new Integer(0)); inter542.addValue(new Integer(0)); inter542.addValue(new Integer(0));
			for(int i = 0; i < inter542.getIntervalValues().size(); i++) {
				assertEquals(cc.getProfileData().getIntervalData(542).get(i), inter542.get(i));
			}
			
			IntervalData inter555 = new IntervalData(interval555.toDate(deuxP.getTimeZone()), 0, 0, 0);
			inter555.addValue(new Integer(1980)); inter555.addValue(new Integer(0)); inter555.addValue(new Integer(1142));
			inter555.addValue(new Integer(0)); inter555.addValue(new Integer(0)); inter555.addValue(new Integer(0));
			for(int i = 0; i < inter555.getIntervalValues().size(); i++) {
				assertEquals(cc.getProfileData().getIntervalData(555).get(i), inter555.get(i));
			}
			
			IntervalData inter568 = new IntervalData(interval568.toDate(deuxP.getTimeZone()), 0, 0, 0);
			inter568.addValue(new Integer(1208)); inter568.addValue(new Integer(808)); inter568.addValue(new Integer(705));
			inter568.addValue(new Integer(297)); inter568.addValue(new Integer(10)); inter568.addValue(new Integer(0));
			for(int i = 0; i < inter568.getIntervalValues().size(); i++) {
				assertEquals(cc.getProfileData().getIntervalData(568).get(i), inter568.get(i));
			}
			
			IntervalData inter569 = new IntervalData(interval569.toDate(deuxP.getTimeZone()), 0, 0, 0);
			inter569.addValue(new Integer(0)); inter569.addValue(new Integer(2148)); inter569.addValue(new Integer(0));
			inter569.addValue(new Integer(781)); inter569.addValue(new Integer(0)); inter569.addValue(new Integer(0));
			for(int i = 0; i < inter569.getIntervalValues().size(); i++) {
				assertEquals(cc.getProfileData().getIntervalData(569).get(i), inter569.get(i));
			}
			
			IntervalData inter586 = new IntervalData(interval586.toDate(deuxP.getTimeZone()), 0, 0, 0);
			inter586.addValue(new Integer(0)); inter586.addValue(new Integer(854)); inter586.addValue(new Integer(0));
			inter586.addValue(new Integer(310)); inter586.addValue(new Integer(0)); inter586.addValue(new Integer(0));
			for(int i = 0; i < inter586.getIntervalValues().size(); i++) {
				assertEquals(cc.getProfileData().getIntervalData(586).get(i), inter586.get(i));
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			fail();
		}
	}

}
