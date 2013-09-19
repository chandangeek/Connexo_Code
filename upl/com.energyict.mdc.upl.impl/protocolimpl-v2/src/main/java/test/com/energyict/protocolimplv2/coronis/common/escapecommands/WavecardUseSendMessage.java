package test.com.energyict.protocolimplv2.coronis.common.escapecommands;

import test.com.energyict.protocolimplv2.coronis.common.WaveFlowConnect;

public class WavecardUseSendMessage extends AbstractEscapeCommand {

    public WavecardUseSendMessage(WaveFlowConnect waveFlowConnect) {
		super(waveFlowConnect);
	}

	@Override
    public EscapeCommandId getEscapeCommandId() {
		return EscapeCommandId.USE_SEND_MESSAGE;
	}

	@Override
    public void parse(byte[] data){
		
	}

	@Override
    public byte[] prepare(){
		return new byte[0];
	}
}