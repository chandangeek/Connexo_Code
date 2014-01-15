package com.energyict.protocolimpl.coronis.waveflow.core.parameter;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;

import java.io.IOException;

public class AlarmConfig extends AbstractParameter {

    int alarmConfig;

    public int getAlarmConfig() {
        return alarmConfig;
    }

    public void setAlarmConfig(int alarmConfig) {
        this.alarmConfig = alarmConfig;
    }

    AlarmConfig(WaveFlow waveFlow) {
        super(waveFlow);
    }

    @Override
    protected ParameterId getParameterId() {
        return ParameterId.AlarmConfig;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        alarmConfig = ProtocolUtils.getInt(data, 0, 1);
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) alarmConfig};
    }

    public void sendAlarmOnWirecutDetection() {
        alarmConfig = alarmConfig | 0x01;
    }

    public void sendAlarmOnBatteryEnd() {
        alarmConfig = alarmConfig | 0x02;
    }

    public void sendAlarmOnLeakLowDetection() {
        alarmConfig = alarmConfig | 0x04;
    }

    public void sendAlarmOnLeakHighDetection() {
        alarmConfig = alarmConfig | 0x08;
    }

    public void sendAlarmOnBackflowDetection() {
        alarmConfig = alarmConfig | 0x10;
    }

    public void sendAlarmOnValveWirecut() {
        alarmConfig = alarmConfig | 0x20;
    }

    public void sendAlarmOnCloseFaultOfValve() {
        alarmConfig = alarmConfig | 0x40;
    }

    public void sendAlarmOnCreditDetection() {
        alarmConfig = alarmConfig | 0x80;
    }

    public void disableAlarmOnWirecutDetection() {
        alarmConfig = alarmConfig & 0xFE;
    }

    public void disableAlarmOnBatteryEnd() {
        alarmConfig = alarmConfig & 0xFD;
    }

    public void disableAlarmOnLeakLowDetection() {
        alarmConfig = alarmConfig & 0xFB;
    }

    public void disableAlarmOnLeakHighDetection() {
        alarmConfig = alarmConfig & 0xF7;
    }

    public void disableAlarmOnBackflowDetection() {
        alarmConfig = alarmConfig & 0xEF;
    }

    public void disableAlarmOnValveWirecut() {
        alarmConfig = alarmConfig & 0xDF;
    }

    public void disableAlarmOnCloseFaultOfValve() {
        alarmConfig = alarmConfig & 0xBF;
    }

    public void disableAlarmOnCreditDetection() {
        alarmConfig = alarmConfig & 0x7F;
    }
}