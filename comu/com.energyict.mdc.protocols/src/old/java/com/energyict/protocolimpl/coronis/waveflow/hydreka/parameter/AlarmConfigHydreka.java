/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.hydreka.parameter;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.AbstractParameter;

import java.io.IOException;

public class AlarmConfigHydreka extends AbstractParameter {

    int alarmConfig;

    public int getAlarmConfig() {
        return alarmConfig;
    }

    public void setAlarmConfig(int alarmConfig) {
        this.alarmConfig = alarmConfig;
    }

    AlarmConfigHydreka(WaveFlow waveFlow) {
        super(waveFlow);
    }

    @Override
    protected AbstractParameter.ParameterId getParameterId() {
        return AbstractParameter.ParameterId.AlarmConfig;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        alarmConfig = ProtocolUtils.getInt(data, 0, 1);
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) alarmConfig};
    }

    public void sendAlarmOnTamper() {
        alarmConfig = alarmConfig | 0x01;
    }

    public void sendAlarmOnModuleBatteryEnd() {
        alarmConfig = alarmConfig | 0x02;
    }

    public void sendAlarmOnLeakage() {
        alarmConfig = alarmConfig | 0x04;
    }

    public void sendAlarmOnProbeBattery() {
        alarmConfig = alarmConfig | 0x08;
    }

    public void disableAlarmOnTamper() {
        alarmConfig = alarmConfig & 0xFE;
    }

    public void disableAlarmOnModuleBatteryEnd() {
        alarmConfig = alarmConfig & 0xFD;
    }

    public void disableAlarmOnLeakage() {
        alarmConfig = alarmConfig & 0xFB;
    }

    public void disableAlarmOnProbeBattery() {
        alarmConfig = alarmConfig & 0xF7;
    }
}