package com.energyict.protocolimpl.iec1107.siemenss4s;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.energyict.protocol.ProfileData;


public class SiemensS4sProfileRecorder {
	
	private ProfileData profileData;
	private Calendar currentMeterTime;
	
	public SiemensS4sProfileRecorder(){
		this.profileData = new ProfileData();
	}
	
	/**
	 * Set the list of channelInfos to the profileDataObject in this RecoderObject
	 * @param channelInfos
	 */
	public void setChannelInfos(List channelInfos) {
		this.profileData.setChannelInfos(channelInfos);
	}

	public void addProfilePart(byte[] profilePart) {
		// TODO Auto-generated method stub
		
	}

	public Date getLastIntervalDate() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Return the profileData object
	 * @return
	 */
	public ProfileData getProfileData() {
		return this.profileData;
	}

	public void setFirstIntervalTime(Calendar meterTime) {
		this.currentMeterTime = meterTime;
	}
}
