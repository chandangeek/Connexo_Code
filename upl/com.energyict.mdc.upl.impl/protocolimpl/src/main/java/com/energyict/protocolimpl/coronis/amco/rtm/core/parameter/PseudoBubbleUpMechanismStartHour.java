package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;

import java.io.IOException;

public class PseudoBubbleUpMechanismStartHour extends AbstractParameter {

    private int hour;
    private int minute;
    private int second;

    PseudoBubbleUpMechanismStartHour(PropertySpecService propertySpecService, RTM rtm, NlsService nlsService) {
        super(propertySpecService, rtm, nlsService);
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.StartOfPseudoBubbleUpMechanism;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        hour = data[0] & 0xFF;
        minute = data[1] & 0xFF;
        second = data[2] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) hour, (byte) minute, (byte) second};
    }
}
