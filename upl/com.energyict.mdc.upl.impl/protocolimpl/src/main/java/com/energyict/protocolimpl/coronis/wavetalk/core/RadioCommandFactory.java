package com.energyict.protocolimpl.coronis.wavetalk.core;

import java.io.IOException;

public class RadioCommandFactory {

	
	private WaveFlow waveFlow;

	// cached
	private FirmwareVersion firmwareVersion=null;
	
	RadioCommandFactory(WaveFlow waveFlow) {
		this.waveFlow = waveFlow;
	}


	
	final FirmwareVersion readFirmwareVersion() throws IOException {
		if (firmwareVersion == null) {
			firmwareVersion = new FirmwareVersion(waveFlow);
			firmwareVersion.invoke();
		}
		return firmwareVersion;
	}
	
}
