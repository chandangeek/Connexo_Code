/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.meterreplacement;

import com.energyict.mdc.sap.soap.webservices.impl.AbstractSapMessage;

import java.time.Instant;

public class RegisterChangeMessage extends AbstractSapMessage {
    private static final String LRN_XMl_NAME = "UtilitiesDevice.Register.UtilitiesMeasurementTaskID";
    private static final String END_DATE_XML_NAME = "UtilitiesDevice.Register.EndDate";

    private String lrn;
    private Instant startDate;
    private Instant endDate;
    private String timeZone;
    private String obis;
    private String recurrenceCode;
    private String divisionCategory;
    private String registerId;


    private RegisterChangeMessage() {
    }

    public String getLrn() {
        return lrn;
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

    public String getTimeZone() {
        return timeZone;
    }

    public String getObis() {
        return obis;
    }

    public String getRecurrenceCode() {
        return recurrenceCode;
    }

    public String getDivisionCategory() {
        return divisionCategory;
    }

    public String getRegisterId() {
        return registerId;
    }

    private void validate() {
        if (lrn == null) {
            addMissingField(LRN_XMl_NAME);
        }
        if (endDate == null) {
            addMissingField(END_DATE_XML_NAME);
        }
    }

    public static class Builder {

        private RegisterChangeMessage registerChangeMessage;

        Builder() {
            registerChangeMessage = new RegisterChangeMessage();
        }

        public void setLrn(String lrn) {
            registerChangeMessage.lrn = lrn;
        }

        public void setEndDate(Instant endDate) {
            registerChangeMessage.endDate = endDate;
        }

        public void setTimeZone(String timeZone) {
            registerChangeMessage.timeZone = timeZone;
        }

        public void setRecurrenceCode(String recurrenceCode) {
            registerChangeMessage.recurrenceCode = recurrenceCode;
        }

        public void setStartDate(Instant startDate) {
            registerChangeMessage.startDate = startDate;
        }

        public void setDivisionCategory(String divisionCategory) {
            registerChangeMessage.divisionCategory = divisionCategory;
        }

        public void setObis(String obis) {
            registerChangeMessage.obis = obis;
        }

        public void setRegisterId(String registerId){
            registerChangeMessage.registerId = registerId;
        };

        public RegisterChangeMessage build() {
            registerChangeMessage.validate();
            return registerChangeMessage;
        }
    }
}
