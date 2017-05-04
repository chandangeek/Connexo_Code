/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.mdc.protocol.api.UnsupportedException;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.RTMFactory;

import java.io.IOException;

public class DriveByEmissionNumber extends AbstractParameter {

    DriveByEmissionNumber(RTM rtm) {
        super(rtm);
    }

    private int number;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.DriveByEmissionNumber;
    }

    @Override
    protected void parse(byte[] data, RTMFactory rtmFactory) throws IOException {
        number = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        throw new UnsupportedException("Cannot set this parameter");
    }
}
