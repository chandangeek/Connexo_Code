package test.com.energyict.protocolimplv2.coronis.waveflow.core.parameter;

import test.com.energyict.protocolimplv2.coronis.common.WaveflowProtocolUtils;
import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

public class HourOfDailyIndexStorage extends AbstractParameter {

    HourOfDailyIndexStorage(WaveFlow waveFlow) {
        super(waveFlow);
    }

    private int hour = 0;

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    @Override
    protected ParameterId getParameterId() {
        return ParameterId.HourOfDailyLogging;
    }

    @Override
    protected void parse(byte[] data) {
        hour = WaveflowProtocolUtils.toInt(data[0]);
    }

    @Override
    protected byte[] prepare() {
        return new byte[]{(byte) getHour()};
    }
}