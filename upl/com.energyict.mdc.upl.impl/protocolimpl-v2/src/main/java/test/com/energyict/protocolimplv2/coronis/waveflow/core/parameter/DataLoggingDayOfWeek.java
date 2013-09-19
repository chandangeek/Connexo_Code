package test.com.energyict.protocolimplv2.coronis.waveflow.core.parameter;

import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

public class DataLoggingDayOfWeek extends AbstractParameter {

    public DataLoggingDayOfWeek(WaveFlow waveFlow) {
        super(waveFlow);
    }

    @Override
    protected ParameterId getParameterId() {
        return ParameterId.DayOfWeek;
    }

    private int dayOfWeek;

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    @Override
    public void parse(byte[] data) {
        dayOfWeek = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() {
        return new byte[]{(byte) dayOfWeek};
    }
}