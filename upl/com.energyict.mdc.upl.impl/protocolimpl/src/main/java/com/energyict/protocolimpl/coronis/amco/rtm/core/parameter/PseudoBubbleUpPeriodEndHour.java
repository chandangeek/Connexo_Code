package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

public class PseudoBubbleUpPeriodEndHour extends AbstractParameter {

    private int hour;

    PseudoBubbleUpPeriodEndHour(PropertySpecService propertySpecService, RTM rtm) {
        super(propertySpecService, rtm);
    }

    final int getHour() {
        return hour;
    }

    final void setHour(int hour) {
        this.hour = hour;
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.EndHourOfPseudoBubbleUpPeriod;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        hour = ProtocolUtils.getInt(data, 0, 1);
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) hour};
    }
}