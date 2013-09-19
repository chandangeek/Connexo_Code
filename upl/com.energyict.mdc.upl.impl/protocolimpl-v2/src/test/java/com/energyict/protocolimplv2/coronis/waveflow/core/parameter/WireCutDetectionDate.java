package com.energyict.protocolimplv2.coronis.waveflow.core.parameter;

import com.energyict.protocolimplv2.coronis.common.TimeDateRTCParser;
import com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 28-feb-2011
 * Time: 11:12:46
 */
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
    protected void parse(byte[] data) {
        eventDate = TimeDateRTCParser.parse(data, getWaveFlow().getTimeZone()).getTime();
    }

    @Override
    protected byte[] prepare() {
        return new byte[0];
    }
}
