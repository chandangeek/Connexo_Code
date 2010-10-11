package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.IOException;

public class EscapeCommandFactory {
	
	private WaveFlow100mW waveFlow100mW;

	EscapeCommandFactory(WaveFlow100mW waveFlow100mW) {
		this.waveFlow100mW = waveFlow100mW;
	}
	
	
	/**
	 * Set the wavecard radio timeout in seconds. this command is used prio to a meterdetect command "0x0C"
	 * @param timeout
	 */
	public void setAndVerifyWavecardRadiotimeout(int timeout) throws IOException {
		WavecardRadioUserTimeout o = new WavecardRadioUserTimeout(waveFlow100mW,timeout);
		o.invoke();
	}
	
}
