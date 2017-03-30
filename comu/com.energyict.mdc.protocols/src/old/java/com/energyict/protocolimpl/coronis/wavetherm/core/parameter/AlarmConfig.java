/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.wavetherm.core.parameter;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.coronis.wavetherm.WaveTherm;

import java.io.IOException;

public class AlarmConfig extends AbstractParameter {

    int alarmConfig;

    public int getAlarmConfig() {
        return alarmConfig;
    }

    public void setAlarmConfig(int alarmConfig) {
        this.alarmConfig = alarmConfig;
    }

    public AlarmConfig(WaveTherm waveTherm) {
        super(waveTherm);
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

    public void sendAlarmOnLowThresholdOnSensor1() {
        alarmConfig = alarmConfig | 0x01;
    }

    public void sendAlarmOnHighThresholdOnSensor1() {
        alarmConfig = alarmConfig | 0x02;
    }

    public void sendAlarmOnEndOfBattery() {
        alarmConfig = alarmConfig | 0x04;
    }

    public void disableAlarmOnLowThresholdOnSensor1() {
        alarmConfig = alarmConfig & 0xFE;
    }

    public void disableAlarmOnHighThresholdOnSensor1() {
        alarmConfig = alarmConfig & 0xFD;
    }

    public void disableAlarmOnEndOfBattery() {
        alarmConfig = alarmConfig & 0xFB;
    }

    public void sendAllAlarms() {
        alarmConfig = 0x07;        //Set b2, b1 and b0
    }

    public void disableAllAlarms() {
        alarmConfig = 0x00;
    }

}