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
    private Instant endDate;
    private String timeZone;

    private RegisterChangeMessage() {
    }

    public String getLrn() {
        return lrn;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public String getTimeZone() {
        return timeZone;
    }

    private void validate() {
        if (lrn == null) {
            addNotValidField(LRN_XMl_NAME);
        }
        if (endDate == null) {
            addNotValidField(END_DATE_XML_NAME);
        }
    }

    public static class Builder {

        private RegisterChangeMessage registerChangeMessage;

        Builder () {
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

        public RegisterChangeMessage build() {
            registerChangeMessage.validate();
            return registerChangeMessage;
        }
    }

}
