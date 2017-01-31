/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.wavesense.core.radiocommand;

import java.util.Date;

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
