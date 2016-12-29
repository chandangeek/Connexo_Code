package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.coronis.amco.rtm.RTM;

import java.io.IOException;

public class NumberOfRepeaters extends AbstractParameter {

    int number;

    NumberOfRepeaters(PropertySpecService propertySpecService, RTM rtm) {
        super(propertySpecService, rtm);
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.NumberOfRepeaters;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        number = ProtocolUtils.getInt(data, 0, 1);
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) number};
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
