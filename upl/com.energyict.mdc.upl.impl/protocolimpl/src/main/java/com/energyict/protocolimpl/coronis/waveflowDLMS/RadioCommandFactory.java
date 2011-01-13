package com.energyict.protocolimpl.coronis.waveflowDLMS;

import java.io.IOException;

import com.energyict.protocolimpl.coronis.core.ProtocolLink;

public class RadioCommandFactory {

	
	private final ProtocolLink protocolLink;

	// cached objects
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
	final int readRSSILevel() throws IOException {
		if (rssiLevel==null) {
			rssiLevel = new RSSILevel(protocolLink);
			rssiLevel.invoke();
		}
		return rssiLevel.getRssiLevel();
	}
	
}
