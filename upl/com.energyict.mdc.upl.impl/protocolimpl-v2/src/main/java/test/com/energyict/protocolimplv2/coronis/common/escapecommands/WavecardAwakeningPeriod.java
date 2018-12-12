package test.com.energyict.protocolimplv2.coronis.common.escapecommands;

import test.com.energyict.protocolimplv2.coronis.common.WaveFlowConnect;

public class WavecardAwakeningPeriod extends AbstractEscapeCommand {

    public WavecardAwakeningPeriod(WaveFlowConnect waveFlowConnect, int awakeningPeriod) {
        super(waveFlowConnect);
        this.awakeningPeriod = awakeningPeriod;
    }

    int awakeningPeriod;

    @Override
    public EscapeCommandId getEscapeCommandId() {
        return EscapeCommandId.AWAKENING_PERIOD;
    }

    @Override
    public void parse(byte[] data){

    }

    @Override
    public byte[] prepare(){
        return new byte[]{(byte) awakeningPeriod};
    }
}