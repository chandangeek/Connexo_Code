/**
 * 
 */
package com.energyict.protocolimpl.edf.trimaran2p.core;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Calendar;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.energyict.cbo.Utils;
import com.energyict.protocol.IntervalData;
import com.energyict.protocolimpl.edf.trimaran2p.Trimaran2P;
import com.energyict.protocolimpl.edf.trimarandlms.axdr.OctetString;

/**
 * @author gna
 *
 */
public class CourbeChargeTest {
	
	private Trimaran2P deuxP;
	private OctetString interval160 = new OctetString(new byte[]{(byte)0x07, (byte)0xD8, (byte)0x06, (byte)0x01, (byte)0xFF, (byte)0x02, (byte)0x32, (byte)0x00});
	private OctetString interval304 = new OctetString(new byte[]{(byte)0x07, (byte)0xD8, (byte)0x06, (byte)0x02, (byte)0xFF, (byte)0x02, (byte)0x32, (byte)0x00});
	private OctetString interval448 = new OctetString(new byte[]{(byte)0x07, (byte)0xD8, (byte)0x06, (byte)0x03, (byte)0xFF, (byte)0x02, (byte)0x32, (byte)0x00});
	private OctetString interval592 = new OctetString(new byte[]{(byte)0x07, (byte)0xD8, (byte)0x06, (byte)0x04, (byte)0xFF, (byte)0x02, (byte)0x32, (byte)0x00});
	private OctetString interval736 = new OctetString(new byte[]{(byte)0x07, (byte)0xD8, (byte)0x06, (byte)0x05, (byte)0xFF, (byte)0x02, (byte)0x32, (byte)0x00});
	private OctetString interval767 = new OctetString(new byte[]{(byte)0x07, (byte)0xD8, (byte)0x06, (byte)0x05, (byte)0xFF, (byte)0x08, (byte)0x00, (byte)0x00});

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
			cc.initCollections();
			assertEquals(2, cc.getProfileData().getChannelInfos().size());
			
			cc.getTrimaranObjectFactory().getParameters().setCcReact(true);		// we must have six channels
			cc.initCollections();
			assertEquals(6, cc.getProfileData().getChannelInfos().size());
			
			for(int i = 0; i < 6; i++){
				file = new File(Utils.class.getResource("/offlineFiles/Object_Values_0406_long" + i + ".bin").getFile());
				fis = new FileInputStream(file);
				ois = new ObjectInputStream(fis);
				values = (int[])ois.readObject();
				cc.addValues(values);
				cc.doParse();
			}
			
			cc.aggregateAndRemoveDuplicates();
			
			IntervalData inter160 = new IntervalData(interval160.toDate(deuxP.getTimeZone()), 0, 0, 0);
			inter160.addValue(new Integer(992)); inter160.addValue(new Integer(0)); inter160.addValue(new Integer(899)); inter160.addValue(new Integer(0)); inter160.addValue(new Integer(0)); inter160.addValue(new Integer(0));
			for(int i = 0; i < inter160.getIntervalValues().size(); i++)
				assertEquals(cc.getProfileData().getIntervalData(160).get(i), inter160.get(i));
			
			IntervalData inter304 = new IntervalData(interval304.toDate(deuxP.getTimeZone()), 0, 0, 0);
			inter304.addValue(new Integer(1026)); inter304.addValue(new Integer(0)); inter304.addValue(new Integer(938)); inter304.addValue(new Integer(0)); inter304.addValue(new Integer(0)); inter304.addValue(new Integer(0));
			for(int i = 0; i < inter304.getIntervalValues().size(); i++)
				assertEquals(cc.getProfileData().getIntervalData(304).get(i), inter304.get(i));
			
			IntervalData inter448 = new IntervalData(interval448.toDate(deuxP.getTimeZone()), 0, 0, 0);
			inter448.addValue(new Integer(921)); inter448.addValue(new Integer(0)); inter448.addValue(new Integer(834)); inter448.addValue(new Integer(0)); inter448.addValue(new Integer(0)); inter448.addValue(new Integer(0));
			for(int i = 0; i < inter448.getIntervalValues().size(); i++)
				assertEquals(cc.getProfileData().getIntervalData(448).get(i), inter448.get(i));
			
			IntervalData inter592 = new IntervalData(interval592.toDate(deuxP.getTimeZone()), 0, 0, 0);
			inter592.addValue(new Integer(222)); inter592.addValue(new Integer(0)); inter592.addValue(new Integer(219)); inter592.addValue(new Integer(0)); inter592.addValue(new Integer(0)); inter592.addValue(new Integer(0));
			for(int i = 0; i < inter592.getIntervalValues().size(); i++)
				assertEquals(cc.getProfileData().getIntervalData(592).get(i), inter592.get(i));
			
			IntervalData inter736 = new IntervalData(interval736.toDate(deuxP.getTimeZone()), 0, 0, 0);
			inter736.addValue(new Integer(206)); inter736.addValue(new Integer(0)); inter736.addValue(new Integer(199)); inter736.addValue(new Integer(0)); inter736.addValue(new Integer(0)); inter736.addValue(new Integer(0));
			for(int i = 0; i < inter736.getIntervalValues().size(); i++)
				assertEquals(cc.getProfileData().getIntervalData(736).get(i), inter736.get(i));
			
			IntervalData inter767 = new IntervalData(interval767.toDate(deuxP.getTimeZone()), 0, 0, 0);
			inter767.addValue(new Integer(225)); inter767.addValue(new Integer(0)); inter767.addValue(new Integer(222)); inter767.addValue(new Integer(0)); inter767.addValue(new Integer(0)); inter767.addValue(new Integer(0));
			for(int i = 0; i < inter767.getIntervalValues().size(); i++)
				assertEquals(cc.getProfileData().getIntervalData(cc.getProfileData().getIntervalDatas().size()-1).get(i), inter767.get(i));
			
			
			
		} catch (FileNotFoundException e) {
			fail();
			e.printStackTrace();
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			fail();
			e.printStackTrace();
		}
	}

}
