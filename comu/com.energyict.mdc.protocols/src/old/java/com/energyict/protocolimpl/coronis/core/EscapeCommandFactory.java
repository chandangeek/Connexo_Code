package com.energyict.protocolimpl.coronis.core;

import java.io.IOException;

public class EscapeCommandFactory {
	
	private ProtocolStackLink protocolStackLink;

	public EscapeCommandFactory(ProtocolStackLink protocolStackLink) {
		this.protocolStackLink = protocolStackLink;
	}

	public byte[] getRadioAddress() throws IOException {
		WavenisRequestRadioAddress o = new WavenisRequestRadioAddress(protocolStackLink);
		o.invoke();
		return o.getRadioAddress();
	}
	
	/**
	 * Set the communication attempt starting with 0 for first attempt.
	 * @param communicationAttemptNr
	 * @throws java.io.IOException
	 */
	public void setWavenisStackCommunicationAttemptNr(int communicationAttemptNr) throws IOException {
		WavenisStackCommunicationAttemptNr o = new WavenisStackCommunicationAttemptNr(protocolStackLink,communicationAttemptNr);
		o.invoke();
	}

	/**
	 * Set the Wavenis stack communication RF response timeout to match the protocols default timeout
	 * @param configRFResponseTimeoutInMs
	 * @throws java.io.IOException
	 */
	public void setWavenisStackConfigRFResponseTimeout(int configRFResponseTimeoutInMs) throws IOException {
		WavenisStackConfigRFResponseTimeout o = new WavenisStackConfigRFResponseTimeout(protocolStackLink,configRFResponseTimeoutInMs);
		o.invoke();
	}

	/**
	 * Set the wavecard radio timeout in seconds. this command is used prio to a meterdetect command "0x0C"
	 * @param timeout
	 */
	public void setAndVerifyWavecardRadiotimeout(int timeout) throws IOException {
		WavecardRadioUserTimeout o = new WavecardRadioUserTimeout(protocolStackLink,timeout);
		o.invoke();
	}

	/**
	 * Set the wavecard wakeup length in milliseconds. We need to set this parameter to 110ms to be able to talk to the DLMS meter
	 * @param wakeupLength in ms
	 */
	public void setAndVerifyWavecardWakeupLength(int wakeupLength) throws IOException {
		WavecardWakeupLength o = new WavecardWakeupLength(protocolStackLink,wakeupLength);
		o.invoke();
	}

	/**
	 * Set the wavecard awakening period in 100ms unities (default is 10 = 1sec). We need to set thios parameter to 110ms to be able to talk to the DLMS meter with the 22 commans REQ_SEND_MESSAGE for the DLMS waveflow 32 command to request multiple obiscodes...
	 * @param awakeningPeriod in ms
	 */
	public void setAndVerifyWavecardAwakeningPeriod(int awakeningPeriod) throws IOException {
		WavecardAwakeningPeriod o = new WavecardAwakeningPeriod(protocolStackLink,awakeningPeriod);
		o.invoke();
	}

	/**
	 * Use the
	 * @throws java.io.IOException
	 */
	public void sendUsingSendMessage() throws IOException {
		WavecardUseSendMessage o = new WavecardUseSendMessage(protocolStackLink);
		o.invoke();
	}
	
	public void sendUsingSendFrame() throws IOException {
		WavecardUseSendFrame o = new WavecardUseSendFrame(protocolStackLink);
		o.invoke();
	}

	public void sendUsingServiceRequest() throws IOException {
		WavecardUseServiceRequest o = new WavecardUseServiceRequest(protocolStackLink);
		o.invoke();
	}
}
