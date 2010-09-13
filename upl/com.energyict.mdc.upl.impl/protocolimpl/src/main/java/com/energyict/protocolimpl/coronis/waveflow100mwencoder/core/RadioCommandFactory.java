package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.IOException;

public class RadioCommandFactory {

	
	private WaveFlow100mW waveFlow100mW;

	RadioCommandFactory(WaveFlow100mW waveFlow100mW) {
		this.waveFlow100mW = waveFlow100mW;
	}


	final EncoderCurrentReading readEncoderCurrentReading() throws IOException {
		EncoderCurrentReading o = new EncoderCurrentReading(waveFlow100mW);
		o.invoke();
		return o;
	}
	
}
