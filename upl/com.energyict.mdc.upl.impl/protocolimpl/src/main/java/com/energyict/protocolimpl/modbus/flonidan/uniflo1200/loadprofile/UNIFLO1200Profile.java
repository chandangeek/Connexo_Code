/**
 * UNIFLO1200Profile.java
 * 
 * Created on 15-dec-2008, 13:30:51 by jme
 * 
 */
package com.energyict.protocolimpl.modbus.flonidan.uniflo1200.loadprofile;

import java.io.IOException;
import java.util.Date;

import com.energyict.protocol.IntervalData;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.UNIFLO1200;


/**
 * @author jme
 *
 */
public class UNIFLO1200Profile {

	public static final int INTERVALLOG 	= 1;
	public static final int DAILYLOG 		= 2;
	public static final int MONTHLOG 		= 3;

	private UNIFLO1200 uniflo1200;
	private UNIFLO1200ProfileInfo profileInfo;
	private UNIFLO1200ProfileData profileData;
	
	public UNIFLO1200Profile(UNIFLO1200 uniflo1200) throws IOException {
		this.uniflo1200 = uniflo1200;
		init();
	}

	private void init() throws IOException {
		this.profileInfo = new UNIFLO1200ProfileInfo(this);
		this.profileData = new UNIFLO1200ProfileData(this);
	}

	public UNIFLO1200 getUniflo1200() {
		return uniflo1200;
	}

	protected int getLoadProfileNumber() {
		return getUniflo1200().getLoadProfileNumber();
	}
 
	public UNIFLO1200ProfileInfo getProfileInfo() {
		return profileInfo;
	}

	public UNIFLO1200ProfileData getProfileData() {
		return profileData;
	}

	public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
		ProfileData pd = new ProfileData();
		if (to == null) to = new Date(Long.MAX_VALUE);
		pd.setChannelInfos(getProfileInfo().getChannelInfos());
		pd.setIntervalDatas(getProfileData().buildIntervalDatas(from, to));
		if (includeEvents) {
			//pd.setMeterEvents()
		}
		
		return pd;
	}

	public int getProfileInterval() {
		return getProfileInfo().getProfileInterval();
	}

	public int getNumberOfChannels() {
		return getProfileInfo().getNumberOfChannels();
	}

}
