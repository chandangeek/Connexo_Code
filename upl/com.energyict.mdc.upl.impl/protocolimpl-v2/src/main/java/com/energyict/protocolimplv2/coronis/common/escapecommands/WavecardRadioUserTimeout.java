package com.energyict.protocolimplv2.coronis.common.escapecommands;

import com.energyict.protocolimplv2.coronis.common.WaveFlowConnect;

public class WavecardRadioUserTimeout extends AbstractEscapeCommand {

    public WavecardRadioUserTimeout(WaveFlowConnect waveFlowConnect, int timeout) {
		super(waveFlowConnect);
		this.timeout=timeout;
	}

	int timeout;
	
	@Override
    public EscapeCommandId getEscapeCommandId() {
		return EscapeCommandId.RADIO_USER_TIMEOUT;
	}

	@Override
    public void parse(byte[] data){
		
	}

	@Override
    public byte[] prepare(){
		return new byte[]{(byte)timeout};
	}
}