package com.energyict.protocolimpl.cm10;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;

public class MeterDemandsTable {
	
	private CM10 cm10Protocol;
	private ProfileData profileData;
	
	public MeterDemandsTable(CM10 cm10Protocol) {
		this.cm10Protocol = cm10Protocol;
	}
	
	public void parse(byte[] data, Date start) throws IOException {
		profileData = new ProfileData();
		addChannelInfos();
		Calendar cal = Calendar.getInstance(cm10Protocol.getTimeZone());
		cal.setTime(start);
		int numberOfChannels = cm10Protocol.getNumberOfChannels();
		int i = 0; 
		int intervalInSeconds = cm10Protocol.getProfileInterval();
		int length = data.length;
		while (i < length) {
			cal.add(Calendar.SECOND, intervalInSeconds);
			IntervalData intervalData = new IntervalData(cal.getTime()); 
			for (int j = 0; j < numberOfChannels; j++) {
				BigDecimal value = new BigDecimal(ProtocolUtils.getLongLE(data, i + (j * 2), 2));
				intervalData.addValue(value);
				cm10Protocol.getLogger().info("channel " + j + ": " + cal.getTime() + ": " + value);
			}
			profileData.addInterval(intervalData);
			i = i + (numberOfChannels * 2); // 2 bytes per channel interval record
		}
	}
	
	public ProfileData getProfileData() throws IOException {
		return profileData;
	}
	
	protected void addChannelInfos() throws IOException {
		int numberOfChannels = cm10Protocol.getNumberOfChannels();
		FullPersonalityTable fullPersonalityTable = cm10Protocol.getFullPersonalityTable();
		int[] multipliers = fullPersonalityTable.getMultipliers();
		for (int i = 0; i < numberOfChannels; i++) {
			ChannelInfo channelInfo = 
				new ChannelInfo(
						i, "CM10_"+(i+1), Unit.get(BaseUnit.UNITLESS), 0, i, new BigDecimal(multipliers[i]));
			profileData.addChannel(channelInfo);			
		}
	}

}
