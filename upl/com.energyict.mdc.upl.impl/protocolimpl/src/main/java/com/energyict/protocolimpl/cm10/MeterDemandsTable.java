package com.energyict.protocolimpl.cm10;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.ProfileData;

public class MeterDemandsTable {
	
	private CM10 cm10Protocol;
	
	public MeterDemandsTable(CM10 cm10Protocol) {
		this.cm10Protocol = cm10Protocol;
	}
	
	public void parse(byte[] data) {
		
	}
	
	public ProfileData getProfileData() throws IOException {
		ProfileData profileData = new ProfileData();
		int numberOfChannels = cm10Protocol.getNumberOfChannels();
		FullPersonalityTable fullPersonalityTable = cm10Protocol.getFullPersonalityTable();
		int[] multipliers = fullPersonalityTable.getMultipliers();
		for (int i = 0; i < numberOfChannels; i++) {
			ChannelInfo channelInfo = 
				new ChannelInfo(
						i, "CM10_"+(i+1), Unit.get(BaseUnit.UNITLESS), 0, i, new BigDecimal(multipliers[i]));
			profileData.addChannel(channelInfo);			
		}
		return profileData;
	}

}
