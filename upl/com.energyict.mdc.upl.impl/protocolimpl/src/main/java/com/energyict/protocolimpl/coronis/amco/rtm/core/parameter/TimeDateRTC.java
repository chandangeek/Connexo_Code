package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;

import java.io.IOException;
import java.util.Calendar;

/**
 * Class can read AND write the meter's clock!
 */
public class TimeDateRTC extends AbstractParameter {

    private Calendar calendar = null;

    public final Calendar getCalendar() {
        return calendar;
    }

    public final void setCalendar(final Calendar calendar) {
        this.calendar = calendar;
    }

    public TimeDateRTC(PropertySpecService propertySpecService, RTM rtm) {
        super(propertySpecService, rtm);
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.Rtc;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        calendar = TimeDateRTCParser.parse(data, getRTM().getTimeZone());
    }

    @Override
    protected byte[] prepare() throws IOException {
        return TimeDateRTCParser.prepare(calendar);
    }
}