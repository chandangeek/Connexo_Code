package com.energyict.protocolimpl.coronis.waveflowDLMS;

import java.io.IOException;

import com.energyict.protocolimpl.coronis.core.ProtocolLink;

public class RadioCommandFactory {

	
	private final ProtocolLink protocolLink;

	// cached objects
	FirmwareVersion firmwareVersion=null;
	
	
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
	
}
