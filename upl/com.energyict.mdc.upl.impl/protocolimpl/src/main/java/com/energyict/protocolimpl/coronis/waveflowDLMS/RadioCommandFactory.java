package com.energyict.protocolimpl.coronis.waveflowDLMS;

import com.energyict.protocolimpl.coronis.core.ProtocolLink;

import java.io.IOException;

public class RadioCommandFactory {

	private final ProtocolLink protocolLink;

	/**
	 * firmware version
	 */
	FirmwareVersion firmwareVersion=null;
	/**
	 * The RSSI level value between 0 and 32
	 */
	RSSILevel rssiLevel=null;

	RadioCommandFactory(ProtocolLink protocolLink) {
		this.protocolLink = protocolLink;
	}

	final FirmwareVersion readFirmwareVersion() throws IOException {
		if (firmwareVersion==null) {
			firmwareVersion = new FirmwareVersion(protocolLink);
			firmwareVersion.invoke();
		}
		return firmwareVersion;
	}
	final double readRSSILevel() throws IOException {
		if (rssiLevel==null) {
			rssiLevel = new RSSILevel(protocolLink);
			rssiLevel.invoke();
		}
		return rssiLevel.getRssiLevelInPercents();
	}
	/**
	 * Set the alarmconfiguration and implicit the alarm route path with the sender's address and path.
	 * @param alarmConfiguration
	 * @throws IOException
	 */
	public final void setAlarmRoute(int alarmConfiguration) throws IOException {
		AlarmRoute o = new AlarmRoute(protocolLink);
		o.setAlarmConfiguration(alarmConfiguration);
		o.invoke();
	}

}
