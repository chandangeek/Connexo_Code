package com.energyict.protocolimpl.coronis.wavesense.core.radiocommand;

import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 7-mrt-2011
 * Time: 15:47:34
 */
public class ThresholdEvent {

    private byte status;
    private Date eventDate;
    private int duration;
    private double integratedValue;

    public int getDuration() {
        return duration;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public double getIntegratedValue() {
        return integratedValue;
    }

    public byte getStatus() {
        return status;
    }

    public ThresholdEvent(int duration, Date eventDate, double integratedValue, byte status) {
        this.duration = duration;
        this.eventDate = eventDate;
        this.integratedValue = integratedValue;
        this.status = status;
    }
}
