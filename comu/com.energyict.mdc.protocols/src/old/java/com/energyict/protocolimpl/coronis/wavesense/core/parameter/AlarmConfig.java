package com.energyict.protocolimpl.coronis.wavesense.core.parameter;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.coronis.wavesense.WaveSense;

import java.io.IOException;

public class AlarmConfig extends AbstractParameter {

    int alarmConfig;

    public int getAlarmConfig() {
        return alarmConfig;
    }

    public void setAlarmConfig(int alarmConfig) {
        this.alarmConfig = alarmConfig;
    }

    AlarmConfig(WaveSense waveSense) {
        super(waveSense);
    }

    @Override
    ParameterId getParameterId() {
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

    public void sendAlarmOnLowThreshold() {
        alarmConfig = alarmConfig | 0x01;
    }

    public void sendAlarmOnHighThreshold() {
        alarmConfig = alarmConfig | 0x02;
    }

    public void sendAlarmOnEndOfBattery() {
        alarmConfig = alarmConfig | 0x04;
    }

    public void sendAlarmOnSensorFault() {
        alarmConfig = alarmConfig | 0x08;
    }

    public void disableAlarmOnLowThreshold() {
        alarmConfig = alarmConfig & 0xFE;
    }

    public void disableAlarmOnHighThreshold() {
        alarmConfig = alarmConfig & 0xFD;
    }

    public void disableAlarmOnEndOfBattery() {
        alarmConfig = alarmConfig & 0xFB;
    }

    public void disableAlarmOnSensorFault() {
        alarmConfig = alarmConfig & 0xF7;
    }

    public void sendAllAlarms() {
        alarmConfig = 0xFF;
    }

    public void disableAllAlarms() {
        alarmConfig = 0x00;
    }

}