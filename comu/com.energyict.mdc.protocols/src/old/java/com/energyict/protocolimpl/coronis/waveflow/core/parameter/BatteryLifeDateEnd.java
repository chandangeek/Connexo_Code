package com.energyict.protocolimpl.coronis.waveflow.core.parameter;

import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;

import java.io.IOException;
import java.util.Calendar;

public class BatteryLifeDateEnd extends AbstractParameter {

    private Calendar calendar;


    final Calendar getCalendar() {
        return calendar;
    }

    BatteryLifeDateEnd(WaveFlow waveFlow) {
        super(waveFlow);
    }

    @Override
    protected ParameterId getParameterId() {
        return ParameterId.BatteryLifeDateEnd;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        try {
            calendar = TimeDateRTCParser.parse(data, getWaveFlow().getTimeZone());
        } catch (Exception e) {
            calendar = Calendar.getInstance();
        }
    }

    @Override
    protected byte[] prepare() throws IOException {
        throw new UnsupportedException();
    }
}