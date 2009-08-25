package com.energyict.protocolimpl.iec1107.siemenss4s;

//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.DLMSUtils;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;


public class SiemensS4sProfileTest {
	
	@Test
	public void getChannelInfoTest(){
		try {
			byte[] channelInfo = DLMSUtils.hexStringToByteArray("3336423042304230");	// Only channel 1
			
			SiemensS4sProfile s4Profile = new SiemensS4sProfile(null);

			List channelInfoList = getExpectedChannelInfoList1();
			
			assertEquals(((ChannelInfo)channelInfoList.get(0)).getChannelId(), ((ChannelInfo)s4Profile.getChannelInfos(channelInfo).get(0)).getChannelId());
			assertEquals(((ChannelInfo)channelInfoList.get(0)).getName(), ((ChannelInfo)s4Profile.getChannelInfos(channelInfo).get(0)).getName());
			assertEquals(((ChannelInfo)channelInfoList.get(0)).getUnit(), ((ChannelInfo)s4Profile.getChannelInfos(channelInfo).get(0)).getUnit());
			
			channelInfo = DLMSUtils.hexStringToByteArray("3336423038414230");			// Channel 1 and Channel 3
			channelInfoList = getExpectedChannelInfoList2();
			
			assertEquals(((ChannelInfo)channelInfoList.get(0)).getChannelId(), ((ChannelInfo)s4Profile.getChannelInfos(channelInfo).get(0)).getChannelId());
			assertEquals(((ChannelInfo)channelInfoList.get(0)).getName(), ((ChannelInfo)s4Profile.getChannelInfos(channelInfo).get(0)).getName());
			assertEquals(((ChannelInfo)channelInfoList.get(0)).getUnit(), ((ChannelInfo)s4Profile.getChannelInfos(channelInfo).get(0)).getUnit());
			assertEquals(((ChannelInfo)channelInfoList.get(1)).getChannelId(), ((ChannelInfo)s4Profile.getChannelInfos(channelInfo).get(1)).getChannelId());
			assertEquals(((ChannelInfo)channelInfoList.get(1)).getName(), ((ChannelInfo)s4Profile.getChannelInfos(channelInfo).get(1)).getName());
			assertEquals(((ChannelInfo)channelInfoList.get(1)).getUnit(), ((ChannelInfo)s4Profile.getChannelInfos(channelInfo).get(1)).getUnit());
			
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

	/**
	 * @return a List with one channelInfo object with ID = 0, type = Import and Unit = kWh
	 */
	private ArrayList getExpectedChannelInfoList1(){
		ArrayList channelObjects = new ArrayList();
		
		ChannelInfo ci = new ChannelInfo(0, "Channel_0 - Import", Unit.get(BaseUnit.WATTHOUR,3));
		channelObjects.add(ci);
		
		return channelObjects;
	}
	
	/**
	 * @return a List with one channelInfo object with ID = 0, type = Import and Unit = kWh
	 */
	private ArrayList getExpectedChannelInfoList2(){
		ArrayList channelObjects = new ArrayList();
		
		ChannelInfo ci = new ChannelInfo(0, "Channel_0 - Import", Unit.get(BaseUnit.WATTHOUR,3));
		channelObjects.add(ci);
		ci = new ChannelInfo(1, "Channel_1 - Export", Unit.get(BaseUnit.VOLTAMPEREHOUR,6));
		channelObjects.add(ci);
		
		return channelObjects;
	}
}
