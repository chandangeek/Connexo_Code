package com.energyict.protocolimplv2.coronis.common.escapecommands;

import com.energyict.protocolimplv2.coronis.common.WaveFlowConnect;

public class WavecardUseSendFrame extends AbstractEscapeCommand {

	public WavecardUseSendFrame(WaveFlowConnect waveFlowConnect) {
		super(waveFlowConnect);
	}

	@Override
    public EscapeCommandId getEscapeCommandId() {
		return EscapeCommandId.USE_SEND_FRAME;
	}

	@Override
    public void parse(byte[] data){
		
	}

	@Override
    public byte[] prepare(){
		return new byte[0];
	}
}