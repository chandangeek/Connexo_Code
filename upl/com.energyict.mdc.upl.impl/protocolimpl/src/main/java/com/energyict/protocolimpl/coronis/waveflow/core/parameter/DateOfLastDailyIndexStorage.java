package com.energyict.protocolimpl.coronis.waveflow.core.parameter;

import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;

import java.io.IOException;
import java.util.Date;

public class DateOfLastDailyIndexStorage extends AbstractParameter {

    DateOfLastDailyIndexStorage(WaveFlow waveFlow) {
        super(waveFlow);
    }

    private Date date;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.DateOfLastDailyIndexStorage;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        date = TimeDateRTCParser.parse(data, getWaveFlow().getTimeZone()).getTime();
    }

    @Override
    protected byte[] prepare() throws IOException {
        throw new UnsupportedException();
    }
}