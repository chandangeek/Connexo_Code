/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.RTMFactory;

import java.io.IOException;

public class MeterModelParameter extends AbstractParameter {

    public MeterModelParameter(RTM rtm) {
        super(rtm);
    }

    private int port = 1;
    private int meterModel = 0;

    public int getMeterModel() {
        return meterModel;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setMeterModel(int meterModel) {
        this.meterModel = meterModel;
    }

    @Override
    ParameterId getParameterId() {
        switch (port) {
            case 1:
                return ParameterId.MeterModelA;
            case 2:
                return ParameterId.MeterModelB;
            case 3:
                return ParameterId.MeterModelC;
            case 4:
                return ParameterId.MeterModelD;
            default:
                return ParameterId.MeterModelA;
        }
    }

    @Override
    protected void parse(byte[] data, RTMFactory rtmFactory) throws IOException {
        meterModel = data[0] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) meterModel};
    }
}