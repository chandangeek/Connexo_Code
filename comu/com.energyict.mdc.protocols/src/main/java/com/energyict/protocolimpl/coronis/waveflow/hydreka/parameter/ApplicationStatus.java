/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.hydreka.parameter;

import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.parameter.AbstractParameter;

import java.io.IOException;

public class ApplicationStatus extends AbstractParameter {

    private int applicationStatus;

    public ApplicationStatus(WaveFlow waveFlow) {
        super(waveFlow);
    }

    @Override
    protected AbstractParameter.ParameterId getParameterId() throws WaveFlowException {
        return AbstractParameter.ParameterId.ApplicationStatus;
    }

    public void setApplicationStatus(int applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public boolean isModuleEndOfBattery() {
        return (applicationStatus & 0x01) == 0x01;
    }

    public boolean isProbeEndOfBattery() {
        return (applicationStatus & 0x02) == 0x02;
    }

    public boolean isLeakage() {
        return (applicationStatus & 0x08) == 0x08;
    }

    public boolean isTamper() {
        return (applicationStatus & 0x20) == 0x20;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        applicationStatus = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) applicationStatus};
    }
}