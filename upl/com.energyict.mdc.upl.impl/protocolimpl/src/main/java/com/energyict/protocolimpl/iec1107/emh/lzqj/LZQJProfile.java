/*
 * A1500Profile.java
 *
 * Created on 23 december 2004, 16:23
 */

package com.energyict.protocolimpl.iec1107.emh.lzqj;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.vdew.AbstractVDEWRegistry;
import com.energyict.protocolimpl.iec1107.vdew.VDEWProfile;
/**
 *
 * @author  Koen
 * Changes:
 * KV 20012005 Initial version
 */
public class LZQJProfile extends VDEWProfile {

	/**
	 * Creates a new instance of LZQJProfile
	 */
	public LZQJProfile(MeterExceptionInfo meterExceptionInfo, ProtocolLink protocolLink, AbstractVDEWRegistry abstractVDEWRegistry) {
		super(meterExceptionInfo, protocolLink, abstractVDEWRegistry, false);
	}

	public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
		Calendar fromCalendar = ProtocolUtils.getCleanCalendar(getProtocolLink().getTimeZone());
		fromCalendar.setTime(lastReading);

		ProfileData profileData = doGetProfileData(fromCalendar, ProtocolUtils.getCalendar(getProtocolLink().getTimeZone()), 1);
		if (includeEvents) {
			List meterEvents = doGetLogBook(fromCalendar, ProtocolUtils.getCalendar(getProtocolLink().getTimeZone()));
			profileData.getMeterEvents().addAll(meterEvents);
			profileData.sort();
		}

		profileData.applyEvents(getProtocolLink().getProfileInterval() / 60);
		return profileData;
	}

	public ProfileData getProfileData(Date fromReading, Date toReading, boolean includeEvents) throws IOException {
		Calendar fromCalendar = ProtocolUtils.getCleanCalendar(getProtocolLink().getTimeZone());
		fromCalendar.setTime(fromReading);
		Calendar toCalendar = ProtocolUtils.getCleanCalendar(getProtocolLink().getTimeZone());
		toCalendar.setTime(toReading);

		ProfileData profileData = doGetProfileData(fromCalendar, toCalendar, 1);
		if (includeEvents) {
			List meterEvents = doGetLogBook(fromCalendar, toCalendar);
			profileData.getMeterEvents().addAll(meterEvents);
			profileData.sort();
		}

		profileData.applyEvents(getProtocolLink().getProfileInterval() / 60);
		return profileData;
	}

} // LZQJProfile

