package com.energyict.dlms.client;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;

public class LoadProfile {

	ProfileData profileData;
	int intervalInSeconds;
	ObisCode loadProfileObiscode;
	
	public LoadProfile(ProfileData profileData,int intervalInSeconds, ObisCode loadProfileObiscode) {
		this.profileData=profileData;
		this.intervalInSeconds=intervalInSeconds;
		this.loadProfileObiscode=loadProfileObiscode;
	}

	public void setIntervalInSeconds(int intervalInSeconds) {
		this.intervalInSeconds = intervalInSeconds;
	}

	public ProfileData getProfileData() {
		return profileData;
	}

	public int getIntervalInSeconds() {
		return intervalInSeconds;
	}

	public ObisCode getLoadProfileObiscode() {
		return loadProfileObiscode;
	}


}
