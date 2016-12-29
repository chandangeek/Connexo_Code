package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

import java.io.IOException;

public class DayOfWeekOrMonth extends AbstractParameter {

    public DayOfWeekOrMonth(PropertySpecService propertySpecService, RTM rtm) {
        super(propertySpecService, rtm);
    }

    /**
     * Day to log data on. (for weekly / monthly data logging mode)
     */
    int day = 0;

    final int getDay() {
        return day;
    }

    final void setDay(int day) {
        this.day = day;
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.DayOfWeekOrMonth;
    }

    @Override
    public void parse(byte[] data) throws IOException {
        day = WaveflowProtocolUtils.toInt(data[0]);
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) getDay()};
    }
}
