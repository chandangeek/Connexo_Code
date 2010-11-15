package com.energyict.protocolimpl.coronis.core;

import java.io.IOException;

public class EscapeCommandFactory {
	
	private ProtocolLink protocolLink;

	public EscapeCommandFactory(ProtocolLink protocolLink) {
		this.protocolLink = protocolLink;
	}
	
	
	/**
	 * Set the wavecard radio timeout in seconds. this command is used prio to a meterdetect command "0x0C"
	 * @param timeout
	 */
	public void setAndVerifyWavecardRadiotimeout(int timeout) throws IOException {
		WavecardRadioUserTimeout o = new WavecardRadioUserTimeout(protocolLink,timeout);
		o.invoke();
	}

	/**
	 * Set the wavecard wakeup length in milliseconds. We need to set thios parameter to 110ms to be able to talk to the DLMS meter
	 * @param wakeupLength in ms
	 */
	public void setAndVerifyWavecardWakeupLength(int wakeupLength) throws IOException {
		WavecardWakeupLength o = new WavecardWakeupLength(protocolLink,wakeupLength);
		o.invoke();
	}
	
	/**
	 * Set the wavecard awakening period in 100ms unities (default is 10 = 1sec). We need to set thios parameter to 110ms to be able to talk to the DLMS meter with the 22 commans REQ_SEND_MESSAGE for the DLMS waveflow 32 command to request multiple obiscodes...
	 * @param wakeupLength in ms
	 */
	public void setAndVerifyWavecardAwakeningPeriod(int awakeningPeriod) throws IOException {
		WavecardAwakeningPeriod o = new WavecardAwakeningPeriod(protocolLink,awakeningPeriod);
		o.invoke();
	}
	
}
