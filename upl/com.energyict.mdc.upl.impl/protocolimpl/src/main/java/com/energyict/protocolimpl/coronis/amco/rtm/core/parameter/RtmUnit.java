package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 12-apr-2011
 * Time: 9:12:14
 */
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

    RtmUnit(PropertySpecService propertySpecService, RTM rtm) {
        super(propertySpecService, rtm);
    }

    public int getUnitNumber() {
        return unitNumber;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
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
