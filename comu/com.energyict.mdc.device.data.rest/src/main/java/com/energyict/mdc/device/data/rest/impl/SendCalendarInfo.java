package com.energyict.mdc.device.data.rest.impl;

import java.math.BigDecimal;
import java.time.Instant;

public class SendCalendarInfo {
    public long allowedCalendarId;
    public Instant releaseDate;
    public Instant activationDate;
    public String type;
    public BigDecimal contract;
    public String calendarUpdateOption;

    public SendCalendarInfo() {

    }
}
