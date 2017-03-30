/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.RTMFactory;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;

import java.io.IOException;

public class MeasurementPeriodMultiplier extends AbstractParameter {

    MeasurementPeriodMultiplier(RTM rtm) {
        super(rtm);
    }

    private int multiplier;

    public int getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(int multiplier) {
        this.multiplier = multiplier;
    }

    @Override
    ParameterId getParameterId() throws WaveFlowException {
        return ParameterId.MeasurementPeriodMultiplier;
    }

    @Override
    protected void parse(byte[] data, RTMFactory rtmFactory) throws IOException {
        multiplier = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) multiplier};
    }
}