/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * UNIFLO1200Profile.java
 *
 * Created on 15-dec-2008, 13:30:51 by jme
 *
 */
package com.energyict.protocolimpl.modbus.flonidan.uniflo1200.profile;

import com.energyict.mdc.protocol.api.device.data.ProfileData;

import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.UNIFLO1200;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.profile.events.UNIFLO1200EventData;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.profile.loadprofile.UNIFLO1200ProfileData;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.profile.loadprofile.UNIFLO1200ProfileInfo;

import java.io.IOException;
import java.util.Date;


/**
 * @author jme
 *
 */
public class UNIFLO1200Profile {

	public static final int DEBUG			= 0;
	public static final int INTERVALLOG 	= 1;
	public static final int DAILYLOG 		= 2;
	public static final int MONTHLOG 		= 3;

	private UNIFLO1200 uniflo1200;
	private UNIFLO1200ProfileInfo profileInfo;
	private UNIFLO1200ProfileData profileData;
	private UNIFLO1200EventData	eventData;

	public UNIFLO1200Profile(UNIFLO1200 uniflo1200) throws IOException {
		this.uniflo1200 = uniflo1200;
		init();
	}

	private void init() throws IOException {
		this.profileInfo = new UNIFLO1200ProfileInfo(this);
		this.profileData = new UNIFLO1200ProfileData(this);
		this.eventData = new UNIFLO1200EventData(this);
	}

	public UNIFLO1200 getUniflo1200() {
		return uniflo1200;
	}

	public int getLoadProfileNumber() {
		return getUniflo1200().getLoadProfileNumber();
	}

	public UNIFLO1200ProfileInfo getProfileInfo() {
		return profileInfo;
	}

	public UNIFLO1200ProfileData getProfileData() {
		return profileData;
	}
	public UNIFLO1200EventData getEventData() {
		return eventData;
	}

	public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
		ProfileData pd = new ProfileData();
		if (to == null) to = new Date(Long.MAX_VALUE);
		pd.setChannelInfos(getProfileInfo().getChannelInfos());
		pd.setIntervalDatas(getProfileData().buildIntervalDatas(from, to));

//		getProfileData().debugMemDump();
//		getEventData().debugMemDump();

		if (includeEvents) {
			pd.setMeterEvents(getEventData().buildEventDatas(from, to));
			pd.applyEvents(getProfileInterval() / 60);
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
