package com.energyict.protocolimplv2.coronis.waveflow.core.parameter;

import com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

public class StartOfPushMechanism extends AbstractParameter {

    private int hour;
    private int minute;
    private int second;

    StartOfPushMechanism(WaveFlow waveFlow) {
        super(waveFlow);
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    @Override
    protected ParameterId getParameterId() {
        return ParameterId.StartOfPushMechanism;
    }

    @Override
    protected void parse(byte[] data) {
        hour = data[0] & 0xFF;
        minute = data[1] & 0xFF;
        second = data[2] & 0xFF;
    }

    @Override
    protected byte[] prepare() {
        return new byte[]{(byte) hour, (byte) minute, (byte) second};
    }
}
