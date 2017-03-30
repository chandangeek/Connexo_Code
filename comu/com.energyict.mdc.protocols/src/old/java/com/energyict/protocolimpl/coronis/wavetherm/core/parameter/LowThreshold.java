/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.wavetherm.core.parameter;


import com.energyict.protocolimpl.coronis.wavetherm.WaveTherm;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

public class LowThreshold extends AbstractParameter {

    public LowThreshold(WaveTherm waveTherm) {
        super(waveTherm);
    }

    private double threshold;

    public void setSensor(int sensor) {
        this.sensor = sensor;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public double getThreshold() {
        return threshold;
    }

    private int sensor = 1;

    @Override
    ParameterId getParameterId() {
        if (sensor == 1) {
            return ParameterId.LowThresholdSensor1;
        } else {
            return ParameterId.LowThresholdSensor2;
        }
    }
    
    @Override
    protected void parse(byte[] data) throws IOException {
        threshold = calcValue(ProtocolTools.getIntFromBytes(data, 0, 2));
    }

    private double calcValue(int rawValue) {
        int sign = ((rawValue & 0xF800) == 0xF800) ? -1 : 1;  //b15 b14 b12 b11 b10 = 11111 ? ==> indicates a negative value
        return sign * (rawValue & 0x07FF) / 16;
    }

    @Override
    protected byte[] prepare() throws IOException {
        int result = (int) Math.round((Math.abs(threshold) * 16));          //Remove the sign, multiply with 16
        result = (threshold < 0) ? result | 0xF800 : result;                //Add 0xF800 to indicate a negative value
        return ProtocolTools.getBytesFromInt(result, 2);
    }
}