/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Unit;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.RTMFactory;

import java.io.IOException;

public class EncoderUnit extends RtmUnit {


    public EncoderUnit(RTM rtm) {
        super(rtm);
    }

    public EncoderUnit(RTM rtm, int port) {
        super(rtm);
        this.port = port;
    }

    @Override
    ParameterId getParameterId() {
        if (port == 1) {
            return ParameterId.EncoderUnitA;
        } else {
            return ParameterId.EncoderUnitB;
        }
    }

    @Override
    public void parse(byte[] data, RTMFactory rtmFactory) throws IOException {
        unitNumber = data[0] & 0xFF;
        scale = (data[1] & 0xFF) - 6;
        scale = (scale == -6) ? 0 : scale;
        unit = convertToUnit(unitNumber);
    }

    private Unit convertToUnit(int number) {
        switch (number) {
            case 0x01:
                return Unit.get(BaseUnit.CUBICMETER, scale);
            case 0x11:
                return Unit.get(BaseUnit.CUBICMETER, scale + 1);
            case 0x21:
                return Unit.get(BaseUnit.CUBICMETER, scale + 2);
            case 0x02:
                return Unit.get(BaseUnit.US_GALLON, scale + 3);
            case 0x03:
                return Unit.get(BaseUnit.GALLON, scale);
            case 0x04:
                return Unit.get(BaseUnit.LITER, scale);
            case 0x05:
                return Unit.get(BaseUnit.CUBICFEET, scale + 2);
            case 0x06:
                return Unit.get(BaseUnit.US_GALLON, scale);
            case 0x07:
                return Unit.get(BaseUnit.UNITLESS, scale + 3);
            case 0x08:
                return Unit.get(BaseUnit.LITER, scale + 3);
            case 0x30:
                return Unit.get(BaseUnit.CUBICFEET, scale);
            case 0x31:
                return Unit.get(BaseUnit.UNITLESS, scale);          //Cubic inches
            case 0x32:
                return Unit.get(BaseUnit.UNITLESS, scale);          //Cubic yards
            case 0x33:
                return Unit.get(BaseUnit.UNITLESS, scale);          //Acre feet
            default:
                return Unit.get(BaseUnit.UNITLESS, 0);              //Unitless
        }
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) unitNumber, (byte) scale};
    }
}
