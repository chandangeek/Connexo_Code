package com.energyict.protocolimpl.cm10;

import java.io.IOException;
import java.util.Date;

import com.energyict.protocol.ProfileData;

public class CM10Profile {
	
	private CM10 cm10Protocol;
	
	public CM10Profile(CM10 cm10Protocol) {
        this.cm10Protocol=cm10Protocol;
    }
	
	public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        ProfileData profileData = new ProfileData();
        return profileData;
	}

}

