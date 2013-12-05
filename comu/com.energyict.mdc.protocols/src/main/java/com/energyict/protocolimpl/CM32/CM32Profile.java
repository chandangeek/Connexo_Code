package com.energyict.protocolimpl.CM32;

import com.energyict.mdc.protocol.api.device.data.ProfileData;

import java.io.IOException;
import java.util.Date;

public class CM32Profile {

	private CM32 cm32Protocol;

	public CM32Profile(CM32 cm32Protocol) {
        this.cm32Protocol=cm32Protocol;
    }

	public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        ProfileData profileData = new ProfileData();
        return profileData;
	}

}

