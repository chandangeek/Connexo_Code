package com.energyict.protocolimpl.coronis.wavetalk.core;

import java.io.IOException;

public class RadioCommandFactory {

	
	private AbstractWaveTalk waveFlow;

	// cached
	private FirmwareVersion firmwareVersion=null;
	
	RadioCommandFactory(AbstractWaveTalk waveFlow) {
		this.waveFlow = waveFlow;
	}

    public double readRSSI() throws IOException {
        RSSILevel rssiLevel = new RSSILevel(waveFlow);
        rssiLevel.set();
        return rssiLevel.getRssiLevel();
    }

	final FirmwareVersion readFirmwareVersion() throws IOException {
		if (firmwareVersion == null) {
			firmwareVersion = new FirmwareVersion(waveFlow);
			firmwareVersion.invoke();
		}
		return firmwareVersion;
	}
	
}
