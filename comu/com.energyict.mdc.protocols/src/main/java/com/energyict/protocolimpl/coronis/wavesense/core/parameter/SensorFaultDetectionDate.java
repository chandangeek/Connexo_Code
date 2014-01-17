package com.energyict.protocolimpl.coronis.wavesense.core.parameter;

import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.wavesense.WaveSense;

import java.io.IOException;
import java.util.Date;

public class SensorFaultDetectionDate extends AbstractParameter {

    private Date eventDate;

    public Date getEventDate() {
        return eventDate;
    }

    SensorFaultDetectionDate(WaveSense waveSense) {
        super(waveSense);
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.SensorFaultDectionDate;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        eventDate = TimeDateRTCParser.parse(data, getWaveSense().getTimeZone()).getTime();
    }

    @Override
    protected byte[] prepare() throws IOException {
        throw new UnsupportedException();
    }
}