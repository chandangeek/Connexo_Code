package com.energyict.protocolimpl.iec1107.siemenss4s;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Calendar;

import org.junit.Test;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.DLMSUtils;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;


public class SiemensS4sProfileRecorderTest {
	
	//TODO Test this with multiple channels
	@Test
	public void addProfilePartTest(){
		
		byte[] channelInfo = DLMSUtils.hexStringToByteArray("3336423042304230");	// Only channel 1
		SiemensS4sProfile s4Profile = new SiemensS4sProfile(null);
		
//		1251126600
		Calendar timeCalendar = Calendar.getInstance();
		timeCalendar.setTimeInMillis(Long.valueOf("1251126600000"));	// 24/08/2009 17:10:00 GMT+02
		byte[] part1 = "0010953000110332317030300010203000100030001069200010692000104920".getBytes();
		byte[] part2 = "0010852100103421001080210010712100103421001027210010423100100921".getBytes();
		byte[] part3 = "0010698000104690001055010010081100103221001095210010262100101721".getBytes();
		byte[] part4 = "0010992000108920001000300010213000102050001020500010640100104290".getBytes();
		
		// In here there will be a day transition and a date interval
//		328061300010		1130001020300010		0030841059200010		9920001010300010		9920
//		001031300		0100130001021300		0103130001090300		0109030001090300		0110332
//		001093		3000108230001052		3000107230001017		5000106730001013		3000103230
//		001		0613000103130001		0813000103330001		0813000108370001		0493000102430
//		0010213000100030		0010603000100130		0010113000106030		0010213000106030
//		0010492000106		9200010792000104		9200010992000103		1300010203000104		030
		try {
			
			SiemensS4sProfileRecorder profileRecorder = new SiemensS4sProfileRecorder(1800);
			profileRecorder.setChannelInfos(s4Profile.getChannelInfos(channelInfo));
			profileRecorder.setFirstIntervalTime(timeCalendar);
			profileRecorder.addProfilePart(part1);
			profileRecorder.addProfilePart(part2);
			profileRecorder.addProfilePart(part3);
			profileRecorder.addProfilePart(part4);
			profileRecorder.getProfileData();
			
		} catch (FlagIEC1107ConnectionException e) {
			e.printStackTrace();
			fail();
		} catch (ConnectionException e) {
			e.printStackTrace();
			fail();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
		
		
	}
}
