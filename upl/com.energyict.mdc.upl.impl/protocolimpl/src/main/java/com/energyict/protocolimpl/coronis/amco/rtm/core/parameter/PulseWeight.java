package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.coronis.amco.rtm.RTM;

import java.io.IOException;

public class PulseWeight extends RtmUnit {

    private int inputChannel;
    private int weight;

    /**
     * This class is only used for the digital pulse model. (not for encoder model)
     */
    public PulseWeight(PropertySpecService propertySpecService, RTM rtm, int inputChannel, NlsService nlsService) {
        super(propertySpecService, rtm, nlsService);
        this.inputChannel = inputChannel;
    }

    public void setUnitNumber(int unitNumber) {
        this.unitNumber = unitNumber;
        parseUnit(unitNumber);
    }

    @Override
    ParameterId getParameterId() {
        switch (inputChannel) {
            case 1:
                return ParameterId.DefinePulseWeightA;
            case 2:
                return ParameterId.DefinePulseWeightB;
            case 3:
                return ParameterId.DefinePulseWeightC;
            case 4:
                return ParameterId.DefinePulseWeightD;
            default:
                return ParameterId.DefinePulseWeightA;
        }
    }

    public int getInputChannel() {
        return inputChannel;
    }

    public void setInputChannel(int inputChannel) {
        this.inputChannel = inputChannel;
    }

    @Override
    public void parse(byte[] data) throws IOException {
        weight = (data[0] & 0xFF) & 0x1F;
        unitNumber = ((data[0] & 0xFF) >> 5);
        parseScaleAndMultiplier(weight);
        parseUnit(unitNumber);
    }

    private void parseUnit(int unitNumber) {
        switch (unitNumber) {
            case 0:
                unit = Unit.get(BaseUnit.CUBICMETER, scale);
                break;
            case 1:
                unit = Unit.get(BaseUnit.LITER, scale);
                break;
            case 2:
                unit = Unit.get(BaseUnit.CUBICFEET, scale);
                break;
            case 3:
                unit = Unit.get(BaseUnit.GALLON, scale);
                break;
            case 4:
                unit = Unit.get(BaseUnit.US_GALLON, scale);
                break;
            default:
                unit = Unit.get("");
                break;
        }
    }

    private void parseScaleAndMultiplier(int weight) {
        if (weight < 10) {
            scale = weight - 5;
            multiplier = 1;
        } else {
            scale = weight - 14;
            multiplier = 5;
        }
    }

    @Override
    protected byte[] prepare() throws IOException {
        if (multiplier == 1) {
            weight = scale + 5;
        }
        if (multiplier == 5) {
            weight = scale + 14;
        }
        unitNumber = convertToUnitNumber(unit);
        return new byte[]{(byte) ((unitNumber << 5) | weight)};
    }

    private int convertToUnitNumber(Unit unit) {
        switch (unit.getDlmsCode()) {
            case BaseUnit.CUBICMETER:
                return 0;
            case BaseUnit.LITER:
                return 1;
            case BaseUnit.CUBICFEET:
                return 2;
            case BaseUnit.GALLON:
                return 3;
            case BaseUnit.US_GALLON:
                return 4;
            default:
                return 0;
        }
    }
}