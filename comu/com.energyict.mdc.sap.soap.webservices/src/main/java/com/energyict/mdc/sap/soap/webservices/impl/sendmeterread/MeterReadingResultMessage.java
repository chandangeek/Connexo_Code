/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.sendmeterread;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.sap.soap.webservices.impl.AbstractSapMessage;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingresultcreaterequest.SmrtMtrMtrRdngDocERPRsltCrteReqRslt;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Optional;

public class MeterReadingResultMessage extends AbstractSapMessage {

    private static final String METER_READING_DATE_XML_NAME = "Result.MeterReadingDate";
    private static final String METER_READING_TIME_XML_NAME = "Result.MeterReadingTime";
    private static final String METER_READING_VALUE_XML_NAME = "Result.MeterReadingResultValue";

    private Instant meterReadingDate;
    private LocalTime meterReadingTime;
    private Instant actualMeterReadingDate;
    private LocalTime actualMeterReadingTime;
    private BigDecimal meterReadingResultValue;
    private String meterReadingTypeCode;

    public MeterReadingResultMessage() {
    }

    public Instant getMeterReadingDate() {
        return meterReadingDate;
    }

    public LocalTime getMeterReadingTime() {
        return meterReadingTime;
    }

    public Instant getActualMeterReadingDate() {
        return actualMeterReadingDate;
    }

    public LocalTime getActualMeterReadingTime() {
        return actualMeterReadingTime;
    }

    public BigDecimal getMeterReadingResultValue() {
        return meterReadingResultValue;
    }

    public String getMeterReadingTypeCode() {
        return meterReadingTypeCode;
    }

    static MeterReadingResultMessage.Builder builder() {
        return new MeterReadingResultMessage().new Builder();
    }

    public class Builder {

        private Builder() {
        }


        public MeterReadingResultMessage.Builder from(SmrtMtrMtrRdngDocERPRsltCrteReqRslt requestMessage) {
            Optional.ofNullable(requestMessage)
                    .ifPresent(singleRequestMessage -> {
                        setValues(requestMessage.getMeterReadingDate(), requestMessage.getMeterReadingTime(), requestMessage.getActualMeterReadingDate(), requestMessage.getActualMeterReadingTime(), getReadingTypeCode(requestMessage), requestMessage
                                .getMeterReadingResultValue());
                    });
            return this;
        }

        public Builder setMeterReadingDate(Instant meterReadingDate) {
            MeterReadingResultMessage.this.meterReadingDate = meterReadingDate;
            return this;
        }


        public Builder setMeterReadingTime(LocalTime meterReadingTime) {
            MeterReadingResultMessage.this.meterReadingTime = meterReadingTime;
            return this;
        }

        public Builder setActualMeterReadingDate(Instant actualMeterReadingDate) {
            MeterReadingResultMessage.this.actualMeterReadingDate = actualMeterReadingDate;
            return this;
        }

        public Builder setActualMeterReadingTime(LocalTime actualMeterReadingTime) {
            MeterReadingResultMessage.this.actualMeterReadingTime = actualMeterReadingTime;
            return this;
        }

        public Builder setReadingTypeCode(String meterReadingTypeCode) {
            MeterReadingResultMessage.this.meterReadingTypeCode = meterReadingTypeCode;
            return this;
        }

        public Builder setReadingResultValue(BigDecimal meterReadingResultValue) {
            MeterReadingResultMessage.this.meterReadingResultValue = meterReadingResultValue;
            return this;
        }

        public void setValues(Instant meterReadingDate, LocalTime meterReadingTime, Instant actualMeterReadingDate, LocalTime actualMeterReadingTime, String meterReadingTypeCode, BigDecimal meterReadingResultValue) {
            setMeterReadingDate(meterReadingDate);
            setMeterReadingTime(meterReadingTime);
            setActualMeterReadingDate(actualMeterReadingDate);
            setActualMeterReadingTime(actualMeterReadingTime);
            setReadingTypeCode(meterReadingTypeCode);
            setReadingResultValue(meterReadingResultValue);
        }


        public MeterReadingResultMessage build() {
            if (meterReadingDate == null) {
                addMissingField(METER_READING_DATE_XML_NAME);
            }
            if (meterReadingTime == null) {
                addMissingField(METER_READING_TIME_XML_NAME);
            }
            if (meterReadingResultValue == null) {
                addMissingField(METER_READING_VALUE_XML_NAME);
            }
            return MeterReadingResultMessage.this;
        }


        private String getReadingTypeCode(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingresultcreaterequest.SmrtMtrMtrRdngDocERPRsltCrteReqRslt meterReadingDocument) {
            return Optional.ofNullable(meterReadingDocument.getMeterReadingTypeCode())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingresultcreaterequest.MeterReadingTypeCode::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

    }
}