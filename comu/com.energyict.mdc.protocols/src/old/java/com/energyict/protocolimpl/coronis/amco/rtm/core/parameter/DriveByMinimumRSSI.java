/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.mdc.protocol.api.UnsupportedException;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.RTMFactory;

import java.io.IOException;

public class DriveByMinimumRSSI extends AbstractParameter {

    DriveByMinimumRSSI(RTM rtm) {
        super(rtm);
    }

    private int minimumRSSI;

    public int getMinimumRSSI() {
        return minimumRSSI;
    }

    public void setMinimumRSSI(int minimumRSSI) {
        this.minimumRSSI = minimumRSSI;
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.DriveByMinimumRSSI;
    }

    @Override
    protected void parse(byte[] data, RTMFactory rtmFactory) throws IOException {
        minimumRSSI = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        throw new UnsupportedException("Cannot set this parameter");
    }
}
