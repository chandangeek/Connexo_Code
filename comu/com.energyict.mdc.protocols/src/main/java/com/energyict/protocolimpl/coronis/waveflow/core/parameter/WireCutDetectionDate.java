/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.parameter;

import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;

import java.io.IOException;
import java.util.Date;

public class WireCutDetectionDate extends AbstractParameter {

    private int inputChannel;
    private Date eventDate;

    WireCutDetectionDate(WaveFlow waveFlow) {
        super(waveFlow);
    }

    WireCutDetectionDate(WaveFlow waveFlow, int inputChannel) {
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
            case 0: return ParameterId.WireCutDetectionDateInputA;
            case 1: return ParameterId.WireCutDetectionDateInputB;
            case 2: return ParameterId.WireCutDetectionDateInputC;
            case 3: return ParameterId.WireCutDetectionDateInputD;
            default: return ParameterId.WireCutDetectionDateInputA;
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
