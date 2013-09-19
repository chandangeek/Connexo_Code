package com.energyict.protocolimplv2.coronis.common.escapecommands;

import com.energyict.protocolimplv2.coronis.common.WaveFlowConnect;

public class WavecardWakeupLength extends AbstractEscapeCommand {

    public WavecardWakeupLength(WaveFlowConnect waveFlowConnect, int wakeupLength) {
		super(waveFlowConnect);
		this.wakeupLength=wakeupLength;
	}

	int wakeupLength;
	
	@Override
    public EscapeCommandId getEscapeCommandId() {
		return EscapeCommandId.WAKEUP_LENGTH;
	}

	@Override
    public void parse(byte[] data){
		
	}

	@Override
    public byte[] prepare(){
		return new byte[]{(byte)(wakeupLength>>8),(byte)wakeupLength};
	}
}