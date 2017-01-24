package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.RTMFactory;

import java.io.IOException;

public class NumberOfRepeaters extends AbstractParameter {

    int number;

    NumberOfRepeaters(RTM rtm) {
        super(rtm);
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.NumberOfRepeaters;
    }

    @Override
    protected void parse(byte[] data, RTMFactory rtmFactory) throws IOException {
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
