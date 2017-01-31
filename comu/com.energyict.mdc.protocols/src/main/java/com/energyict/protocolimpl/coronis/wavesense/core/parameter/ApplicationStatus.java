/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.wavesense.core.parameter;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.coronis.wavesense.WaveSense;

import java.io.IOException;

public class ApplicationStatus extends AbstractParameter {

    int status;

    ApplicationStatus(WaveSense waveSense) {
        super(waveSense);
    }

    public final int getStatus() {
        return status;
    }

    public final void setStatus(int status) {
        this.status = status;
    }

    @Override
    protected ParameterId getParameterId() {
        return ParameterId.ApplicationStatus;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        status = ProtocolUtils.getInt(data, 0, 1);
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) status};
    }

    public boolean resetDetected() {
        return (status & 0x80) == 0x80;
    }

    public boolean lowThresholdDetected() {
        return (status & 0x10) == 0x10;
    }

    public boolean highThresholdDetected() {
        return (status & 0x08) == 0x08;
    }

    public boolean sensorFaultDetected() {
        return (status & 0x02) == 0x02;
    }

    public boolean endOfBatteryLifeDetected() {
        return (status & 0x01) == 0x01;
    }
}