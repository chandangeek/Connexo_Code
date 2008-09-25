package com.energyict.protocolimpl.cm10;

import java.util.Date;

import com.energyict.protocol.ProfileData;

public class MeterDemandsTable {
	
	private CM10 cm10Protocol;
	
	public MeterDemandsTable(CM10 cm10Protocol) {
		this.cm10Protocol = cm10Protocol;
	}
	
	public void parse(byte[] data) {
		
	}
	
	public ProfileData getProfileData() {
		ProfileData profileData = new ProfileData();
		return profileData;
	}

}
