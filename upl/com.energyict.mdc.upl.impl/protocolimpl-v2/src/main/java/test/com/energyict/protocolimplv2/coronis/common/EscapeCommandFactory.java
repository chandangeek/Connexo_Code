package test.com.energyict.protocolimplv2.coronis.common;

import test.com.energyict.protocolimplv2.coronis.common.escapecommands.*;

public class EscapeCommandFactory {

    private WaveFlowConnect waveFlowConnect;

    public EscapeCommandFactory(WaveFlowConnect waveFlowConnect) {
        this.waveFlowConnect = waveFlowConnect;
    }

    public byte[] getRadioAddress() {
        WavenisRequestRadioAddress o = new WavenisRequestRadioAddress(waveFlowConnect);
        o.invoke();
        return o.getRadioAddress();
    }

    /**
     * Set the communication attempt starting with 0 for first attempt.
     *
     * @param communicationAttemptNr
     */
    public void setWavenisStackCommunicationAttemptNr(int communicationAttemptNr) {
        WavenisStackCommunicationAttemptNr o = new WavenisStackCommunicationAttemptNr(waveFlowConnect, communicationAttemptNr);
        o.invoke();
    }

    /**
     * Set the Wavenis stack communication RF response timeout to match the protocols default timeout
     *
     * @param configRFResponseTimeoutInMs
     */
    public void setWavenisStackConfigRFResponseTimeout(int configRFResponseTimeoutInMs) {
        WavenisStackConfigRFResponseTimeout o = new WavenisStackConfigRFResponseTimeout(waveFlowConnect, configRFResponseTimeoutInMs);
        o.invoke();
    }

    /**
     * Set the wavecard radio timeout in seconds. this command is used prio to a meterdetect command "0x0C"
     *
     * @param timeout
     */
    public void setAndVerifyWavecardRadiotimeout(int timeout) {
        WavecardRadioUserTimeout o = new WavecardRadioUserTimeout(waveFlowConnect, timeout);
        o.invoke();
    }

    /**
     * Set the wavecard wakeup length in milliseconds. We need to set this parameter to 110ms to be able to talk to the DLMS meter
     *
     * @param wakeupLength in ms
     */
    public void setAndVerifyWavecardWakeupLength(int wakeupLength) {
        WavecardWakeupLength o = new WavecardWakeupLength(waveFlowConnect, wakeupLength);
        o.invoke();
    }

    /**
     * Set the wavecard awakening period in 100ms unities (default is 10 = 1sec). We need to set thios parameter to 110ms to be able to talk to the DLMS meter with the 22 commans REQ_SEND_MESSAGE for the DLMS waveflow 32 command to request multiple obiscodes...
     *
     * @param awakeningPeriod in ms
     */
    public void setAndVerifyWavecardAwakeningPeriod(int awakeningPeriod) {
        WavecardAwakeningPeriod o = new WavecardAwakeningPeriod(waveFlowConnect, awakeningPeriod);
        o.invoke();
    }

    /**
     * Use the
     */
    public void sendUsingSendMessage() {
        WavecardUseSendMessage o = new WavecardUseSendMessage(waveFlowConnect);
        o.invoke();
    }

    public void sendUsingSendFrame() {
        WavecardUseSendFrame o = new WavecardUseSendFrame(waveFlowConnect);
        o.invoke();
    }

    public void sendUsingServiceRequest() {
        WavecardUseServiceRequest o = new WavecardUseServiceRequest(waveFlowConnect);
        o.invoke();
    }
}