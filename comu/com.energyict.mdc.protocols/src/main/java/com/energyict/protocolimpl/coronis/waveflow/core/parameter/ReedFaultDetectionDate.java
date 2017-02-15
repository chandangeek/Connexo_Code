/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.parameter;

import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;

import java.io.IOException;
import java.util.Date;

public class ReedFaultDetectionDate extends AbstractParameter {

    private int inputChannel;
    private Date eventDate;

    ReedFaultDetectionDate(WaveFlow waveFlow) {
        super(waveFlow);
    }

    ReedFaultDetectionDate(WaveFlow waveFlow, int inputChannel) {
        super(waveFlow);
        this.inputChannel = inputChannel;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public int getInputChannel() {
        return inputChannel;
    }

    @Override
    protected ParameterId getParameterId() {
        switch (inputChannel) {
            case 0: return ParameterId.ReedFaultDetectionDateInputA;
            case 1: return ParameterId.ReedFaultDetectionDateInputB;
            default: return ParameterId.ReedFaultDetectionDateInputA;
        }
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        eventDate = TimeDateRTCParser.parse(data, getWaveFlow().getTimeZone()).getTime();
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[0];
    }
}
