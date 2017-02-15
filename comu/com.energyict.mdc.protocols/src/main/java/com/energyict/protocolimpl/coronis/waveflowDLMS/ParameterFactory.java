/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflowDLMS;

import com.energyict.protocolimpl.coronis.core.ProtocolLink;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class ParameterFactory {


	private final ProtocolLink protocolLink;

	ParameterFactory(ProtocolLink protocolLink) {
		this.protocolLink = protocolLink;
	}


	final public Date readTimeDateRTC() throws IOException {
		TimeDateRTC o = new TimeDateRTC(protocolLink);
		o.read();
		return o.getCalendar().getTime();
	}

	final public void writeTimeDateRTC(final Date date) throws IOException {
		TimeDateRTC o = new TimeDateRTC(protocolLink);
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT")); //protocolLink.getTimeZone());
		calendar.setTime(date);
		o.setCalendar(calendar);
		o.write();
	}

	final public int readOperatingMode() throws IOException {
		OperatingMode o = new OperatingMode(protocolLink);
		o.read();
		return o.getOperatingMode();
	}
	final public void writeOperatingMode(int operatingMode) throws IOException {
		OperatingMode o = new OperatingMode(protocolLink);
		o.setOperatingMode(operatingMode);
		o.write();
	}

	final public int readApplicationStatus() throws IOException {
		ApplicationStatus o = new ApplicationStatus(protocolLink);
		o.read();
		return o.getStatus();
	}

	final public void writeApplicationStatus(int applicationStatus) throws IOException {
		ApplicationStatus o = new ApplicationStatus(protocolLink);
		o.setStatus(applicationStatus);
		o.write();
	}

	final public int readAlarmConfiguration() throws IOException {
		AlarmConfiguration o = new AlarmConfiguration(protocolLink);
		o.read();
		return o.getAlarmConfiguration();
	}

	final public void writeAlarmConfiguration(int alarmConfiguration) throws IOException {
		AlarmConfiguration o = new AlarmConfiguration(protocolLink);
		o.setAlarmConfiguration(alarmConfiguration);
		o.write();
	}

	final public int readVersion() throws IOException {
		Version o = new Version(protocolLink);
		o.read();
		return o.getVersion();
	}



}
