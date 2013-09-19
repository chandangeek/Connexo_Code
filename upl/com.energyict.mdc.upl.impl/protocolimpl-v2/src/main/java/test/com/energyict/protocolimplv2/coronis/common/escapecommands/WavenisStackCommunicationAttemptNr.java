package test.com.energyict.protocolimplv2.coronis.common.escapecommands;

import test.com.energyict.protocolimplv2.coronis.common.WaveFlowConnect;

public class WavenisStackCommunicationAttemptNr extends AbstractEscapeCommand {

    public WavenisStackCommunicationAttemptNr(WaveFlowConnect waveFlowConnect, int communicationAttemptNr) {
        super(waveFlowConnect);
        this.communicationAttemptNr = communicationAttemptNr;
    }

    int communicationAttemptNr;

    @Override
    public EscapeCommandId getEscapeCommandId() {
        return EscapeCommandId.WAVENIS_COMMUNICATION_ATTEMPT_NR;
    }

    @Override
    public void parse(byte[] data){

    }

    @Override
    public byte[] prepare(){
        return new byte[]{(byte) communicationAttemptNr};
    }
}