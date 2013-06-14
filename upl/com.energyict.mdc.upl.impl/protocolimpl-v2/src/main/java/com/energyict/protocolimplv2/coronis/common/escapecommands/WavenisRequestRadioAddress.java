package com.energyict.protocolimplv2.coronis.common.escapecommands;

import com.energyict.protocolimplv2.coronis.common.WaveFlowConnect;

public class WavenisRequestRadioAddress extends AbstractEscapeCommand {

    public WavenisRequestRadioAddress(WaveFlowConnect waveFlowConnect) {
        super(waveFlowConnect);
    }

    byte[] radioAddress;

    public final byte[] getRadioAddress() {
        return radioAddress;
    }

    public
    @Override
    EscapeCommandId getEscapeCommandId() {
        return EscapeCommandId.WAVENIS_REQUEST_RADIO_ADDRESS;
    }

    @Override
    public void parse(byte[] data){
        radioAddress = data;
    }

    @Override
    public byte[] prepare(){
        return new byte[0];
    }
}