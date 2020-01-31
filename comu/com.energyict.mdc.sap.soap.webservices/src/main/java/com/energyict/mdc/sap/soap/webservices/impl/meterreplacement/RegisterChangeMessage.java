/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.meterreplacement;

import java.time.Instant;

public class RegisterChangeMessage {

    private String lrn;
    private Instant startDate;
    private Instant endDate;
    private String timeZone;
    private String obis;
    private String recurrenceCode;
    private String divisionCategory;

    public boolean isValid() {
        return lrn != null && endDate != null;
    }

    public String getLrn() {
        return lrn;
    }

    public void setLrn(String lrn) {
        this.lrn = lrn;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getObis() {
        return obis;
    }

    public void setObis(String obis) {
        this.obis = obis;
    }

    public String getRecurrenceCode() {
        return recurrenceCode;
    }

    public void setRecurrenceCode(String recurrenceCode) {
        this.recurrenceCode = recurrenceCode;
    }

    public String getDivisionCategory() {
        return divisionCategory;
    }

    public void setDivisionCategory(String divisionCategory) {
        this.divisionCategory = divisionCategory;
    }
}
