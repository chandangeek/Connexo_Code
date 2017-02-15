/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.mdc.common.Unit;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.RTMFactory;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;

import java.io.IOException;

public class RtmUnit extends AbstractParameter {

    protected Unit unit = Unit.get("");
    protected int multiplier = 1;
    protected int port;
    protected int unitNumber;
    protected int scale;

    public void setUnitNumber(int unitNumber) {
        this.unitNumber = unitNumber;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(int multiplier) {
        this.multiplier = multiplier;
    }

    public Unit getUnit() {
        return unit;
    }

    RtmUnit(RTM rtm) {
        super(rtm);
    }

    public int getUnitNumber() {
        return unitNumber;
    }

    @Override
    protected void parse(byte[] data, RTMFactory rtmFactory) throws IOException {
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[0];
    }

    @Override
    ParameterId getParameterId() throws WaveFlowException {
        return null;
    }
}
