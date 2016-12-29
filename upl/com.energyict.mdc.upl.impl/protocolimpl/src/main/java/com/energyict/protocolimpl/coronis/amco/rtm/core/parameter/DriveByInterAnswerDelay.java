package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 7-apr-2011
 * Time: 16:45:00
 */
public class DriveByInterAnswerDelay extends AbstractParameter {

    DriveByInterAnswerDelay(PropertySpecService propertySpecService, RTM rtm) {
        super(propertySpecService, rtm);
    }

    private int hours;
    private int minutes;
    private int seconds;

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    @Override
    ParameterId getParameterId() {
        return ParameterId.DriveByInterAnswerDelay;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        hours = data[0] & 0xFF;
        minutes = data[1] & 0xFF;
        seconds = data[2] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[]{(byte) hours, (byte) minutes, (byte) seconds};
    }
}
