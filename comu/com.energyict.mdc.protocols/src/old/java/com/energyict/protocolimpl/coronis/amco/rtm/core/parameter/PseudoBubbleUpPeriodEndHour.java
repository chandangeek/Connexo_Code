package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.RTMFactory;

import java.io.IOException;

public class PseudoBubbleUpPeriodEndHour extends AbstractParameter {

    private int hour;

    PseudoBubbleUpPeriodEndHour(RTM rtm) {
        super(rtm);
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
    protected void parse(byte[] data, RTMFactory rtmFactory) throws IOException {
        hour = ProtocolUtils.getInt(data, 0, 1);
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) hour};
    }
}