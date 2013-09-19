package test.com.energyict.protocolimplv2.coronis.waveflow.core.parameter;

import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

public class DataLoggingMinuteOfMeasurement extends AbstractParameter {

    public DataLoggingMinuteOfMeasurement(WaveFlow waveFlow) {
        super(waveFlow);
    }
    private int minuteOfMeasurement;

    @Override
    protected ParameterId getParameterId() {
        return ParameterId.MinuteOfMeasurement;
    }

    public void setMinuteOfMeasurement(int minuteOfMeasurement) {
        this.minuteOfMeasurement = minuteOfMeasurement;
    }

    public int getMinuteOfMeasurement() {
        return minuteOfMeasurement;
    }

    @Override
    public void parse(byte[] data) {
        minuteOfMeasurement = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() {
        return new byte[]{(byte) minuteOfMeasurement};
    }
}