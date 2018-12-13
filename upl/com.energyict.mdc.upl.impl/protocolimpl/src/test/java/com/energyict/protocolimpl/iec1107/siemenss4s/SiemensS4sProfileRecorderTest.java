package com.energyict.protocolimpl.iec1107.siemenss4s;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Calendar;

import org.junit.Test;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.IntervalValue;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.siemenss4s.objects.S4sObjectUtils;

public class SiemensS4sProfileRecorderTest {

	// TODO Test this with multiple channels
	@Test
	public void addProfilePartTest() {

		byte[] channelInfo = S4sObjectUtils.hexStringToByteArray("3336423042304230");// Only channel 1
		SiemensS4sProfile s4Profile = new SiemensS4sProfile(null);

		Calendar timeCalendar = Calendar.getInstance();
		timeCalendar.setTimeInMillis(Long.valueOf("1251194275000"));
		
		byte[] part1 = "0010035000101890001068700010148000109280001080010010860100106901".getBytes();
		byte[] part2 = "0010213000101130001021300010213000100130001041300010953000109840".getBytes();
		byte[] part3 = "0010143000106330001103324280323000102230001061300010213084105130".getBytes();
		byte[] part4 = "0010585000105140001069300010404000104930001047300010683000102630".getBytes();
		byte[] part5 = "0010423100100921001048210010892100101821001057110010290100103790".getBytes();
		byte[] part6 = "0010262100101721001085210010342100108021001071210010342100102721".getBytes();
		byte[] part7 = "0010640100104290001069800010469000105501001008110010322100109521".getBytes();
		byte[] part8 = "0010103000109920001099200010892000100030001021300010205000102050".getBytes();
		byte[] part9 = "0010903000110332328061300010113000102030001000308410592000109920".getBytes();
		byte[] part10 = "0010133000103230001031300010013000102130001031300010903000109030".getBytes();
		byte[] part11 = "0010493000102430001093300010823000105230001072300010175000106730".getBytes();
		byte[] part12 = "0010213000106030001061300010313000108130001033300010813000108370".getBytes();
		byte[] part13 = "0010203000104030001021300010003000106030001001300010113000106030".getBytes();
		byte[] part14 = "0010792000105920001049200010692000107920001049200010992000103130".getBytes();
		byte[] part15 = "0011033222807920001069200010692000103920001069200010592000106920".getBytes();
		byte[] part16 = "0010173000104430001064300010333000109130001081300010433000102230".getBytes();
		byte[] part17 = "0010673000104630001006300010473000104530001064300010114000108860".getBytes();
		byte[] part18 = "0010143000104330001005300010073000100930001078300010283000101930".getBytes();
		byte[] part19 = "0010503000109130001033300010833000100430001083300010433000101430".getBytes();
		byte[] part20 = "0010192000106920001039200010292000103920001059200010203000100030".getBytes();
		byte[] part21 = "1280403000109920001069200010492000103920001059200010292000105920".getBytes();
		byte[] part22 = "0010583000104730001087300010453000106530001005300010723000110332".getBytes();

		try {

			SiemensS4sProfileRecorder profileRecorder = new SiemensS4sProfileRecorder(1800);
			profileRecorder.setChannelInfos(s4Profile.getChannelInfos(channelInfo));
			profileRecorder.setFirstIntervalTime(timeCalendar);
			profileRecorder.addProfilePart(part1);
			profileRecorder.addProfilePart(part2);
			profileRecorder.addProfilePart(part3);
			profileRecorder.addProfilePart(part4);
			profileRecorder.addProfilePart(part5);
			profileRecorder.addProfilePart(part6);
			profileRecorder.addProfilePart(part7);
			profileRecorder.addProfilePart(part8);
			profileRecorder.addProfilePart(part9);
			profileRecorder.addProfilePart(part10);
			profileRecorder.addProfilePart(part11);
			profileRecorder.addProfilePart(part12);
			profileRecorder.addProfilePart(part13);
			profileRecorder.addProfilePart(part14);
			profileRecorder.addProfilePart(part15);
			profileRecorder.addProfilePart(part16);
			profileRecorder.addProfilePart(part17);
			profileRecorder.addProfilePart(part18);
			profileRecorder.addProfilePart(part19);
			profileRecorder.addProfilePart(part20);
			profileRecorder.addProfilePart(part21);
			profileRecorder.addProfilePart(part22);
			profileRecorder.getProfileData();
			
			timeCalendar.setTimeInMillis(Long.valueOf("1251163800000"));
			
			// Interval 16 should be at 25/08/2009 03:30:00, the value should be 315 and the status should 4(clock set)
			assertEquals(timeCalendar.getTimeInMillis(), profileRecorder.getProfileData().getIntervalData(16).getEndTime().getTime());
			assertEquals(IntervalStateBits.SHORTLONG, profileRecorder.getProfileData().getIntervalData(16).getEiStatus());
			assertEquals(Integer.valueOf(315),((IntervalValue)profileRecorder.getProfileData().getIntervalData(16).getIntervalValues().get(0)).getNumber());

			byte[] multiplePart1 = "0020035103520020189118920020687168720020148114820020928192820020".getBytes();
			byte[] multiplePart2 = "8001800200208601860200206901860200202131213200201131113200202131".getBytes();
			byte[] multiplePart3 = "2132002021312132002001310132002041314132002095319532002098419842".getBytes();
			channelInfo = S4sObjectUtils.hexStringToByteArray("3336333642304230");// Only channel 1
			
			profileRecorder = new SiemensS4sProfileRecorder(1800);
			profileRecorder.setChannelInfos(s4Profile.getChannelInfos(channelInfo));
			timeCalendar.setTimeInMillis(Long.valueOf("1251194275000"));
			profileRecorder.setFirstIntervalTime(timeCalendar);
			
			profileRecorder.addProfilePart(multiplePart1);
			profileRecorder.addProfilePart(multiplePart2);
			profileRecorder.addProfilePart(multiplePart3);
			
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
