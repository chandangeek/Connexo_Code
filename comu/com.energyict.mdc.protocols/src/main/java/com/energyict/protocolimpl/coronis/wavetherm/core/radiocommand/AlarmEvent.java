package com.energyict.protocolimpl.coronis.wavetherm.core.radiocommand;

import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 7-mrt-2011
 * Time: 15:47:34
 */
public class AlarmEvent {

    private int sensorNumber;
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

    public int getSensorNumber() {
        return sensorNumber;
    }

    public AlarmEvent(int duration, Date eventDate, double integratedValue, int sensorNumber) {
        this.duration = duration;
        this.eventDate = eventDate;
        this.integratedValue = integratedValue;
        this.sensorNumber = sensorNumber;
    }
}
