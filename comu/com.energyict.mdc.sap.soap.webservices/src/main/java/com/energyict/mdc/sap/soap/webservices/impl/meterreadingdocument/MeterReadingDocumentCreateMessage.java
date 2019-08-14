/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.sap.soap.webservices.SAPMeterReadingDocumentReason;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;

import java.time.Instant;
import java.util.Optional;

public class MeterReadingDocumentCreateMessage {

    private String id;
    private String deviceId;
    private String lrn;
    private String readingReasonCode;
    private Instant scheduledMeterReadingDate;

    private MeterReadingDocumentCreateMessage() {
    }

    public String getId() {
        return id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getLrn() {
        return lrn;
    }

    public String getReadingReasonCode() {
        return readingReasonCode;
    }

    public Instant getScheduledMeterReadingDate() {
        return scheduledMeterReadingDate;
    }

    public boolean isValid() {
        if (id != null && deviceId != null && lrn != null && readingReasonCode != null && scheduledMeterReadingDate != null) {
            return true;
        }
        return false;
    }

    public boolean isReasonCodeSupported(boolean bulk) {
        return bulk ? isBulkSupported() : isSingleSupported();
    }

    public boolean isSingleSupported() {
        return findReasonCode()
                .map(SAPMeterReadingDocumentReason::isSingle)
                .orElse(false);
    }

    public boolean isBulkSupported() {
        return findReasonCode()
                .map(SAPMeterReadingDocumentReason::isBulk)
                .orElse(false);
    }

    private Optional<SAPMeterReadingDocumentReason> findReasonCode() {
        return WebServiceActivator.METER_READING_REASONS
                .stream()
                .filter(reasonCode -> reasonCode.getCode().equals(readingReasonCode))
                .findFirst();
    }

    static MeterReadingDocumentCreateMessage.Builder builder() {
        return new MeterReadingDocumentCreateMessage().new Builder();
    }

    public class Builder {

        private Builder() {
        }

        public MeterReadingDocumentCreateMessage.Builder from(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreaterequest.SmrtMtrMtrRdngDocERPCrteReqMtrRdngDoc requestMessage) {
            Optional.ofNullable(requestMessage)
                    .ifPresent(bulkRequestMessage -> setValues(getId(bulkRequestMessage),
                            getDeviceId(bulkRequestMessage), getLrn(bulkRequestMessage),
                            getReadingReasonCode(bulkRequestMessage), bulkRequestMessage.getScheduledMeterReadingDate()));
            return this;
        }

        public MeterReadingDocumentCreateMessage.Builder from(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreaterequest.SmrtMtrMtrRdngDocERPCrteReqMtrRdngDoc requestMessage) {
            Optional.ofNullable(requestMessage)
                    .ifPresent(singleRequestMessage -> {
                        setValues(getId(singleRequestMessage), getDeviceId(singleRequestMessage),
                                getLrn(singleRequestMessage), getReadingReasonCode(singleRequestMessage),
                                singleRequestMessage.getScheduledMeterReadingDate());
                    });
            return this;
        }

        public Builder setId(String id) {
            MeterReadingDocumentCreateMessage.this.id = id;
            return this;
        }

        public Builder setDeviceId(String deviceId) {
            MeterReadingDocumentCreateMessage.this.deviceId = deviceId;
            return this;
        }

        public Builder setLrn(String lrn) {
            MeterReadingDocumentCreateMessage.this.lrn = lrn;
            return this;
        }

        public Builder setReadingReasonCode(String readingReasonCode) {
            MeterReadingDocumentCreateMessage.this.readingReasonCode = readingReasonCode;
            return this;
        }

        public Builder setScheduledMeterReadingDate(Instant scheduledMeterReadingDate) {
            MeterReadingDocumentCreateMessage.this.scheduledMeterReadingDate = scheduledMeterReadingDate;
            return this;
        }

        public MeterReadingDocumentCreateMessage build() {
            return MeterReadingDocumentCreateMessage.this;
        }

        void setValues(String id, String deviceId, String lrn, String code,
                       Instant date) {
            setId(id);
            setDeviceId(deviceId);
            setLrn(lrn);
            setReadingReasonCode(code);
            setScheduledMeterReadingDate(date);
        }

        private String getId(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreaterequest.SmrtMtrMtrRdngDocERPCrteReqMtrRdngDoc meterReadingDocument) {
            return Optional.ofNullable(meterReadingDocument.getID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreaterequest.MeterReadingDocumentID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getId(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreaterequest.SmrtMtrMtrRdngDocERPCrteReqMtrRdngDoc meterReadingDocument) {
            return Optional.ofNullable(meterReadingDocument.getID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreaterequest.MeterReadingDocumentID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getDeviceId(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreaterequest.SmrtMtrMtrRdngDocERPCrteReqMtrRdngDoc meterReadingDocument) {
            return Optional.ofNullable(meterReadingDocument.getUtiltiesMeasurementTask())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreaterequest.SmrtMtrMtrRdngDocERPCrteReqUtilsMsmtTsk::getUtiltiesDevice)
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreaterequest.SmrtMtrMtrRdngDocERPCrteReqUtilsDvce::getUtilitiesDeviceID)
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreaterequest.UtilitiesDeviceID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getDeviceId(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreaterequest.SmrtMtrMtrRdngDocERPCrteReqMtrRdngDoc meterReadingDocument) {
            return Optional.ofNullable(meterReadingDocument.getUtiltiesMeasurementTask())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreaterequest.SmrtMtrMtrRdngDocERPCrteReqUtilsMsmtTsk::getUtiltiesDevice)
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreaterequest.SmrtMtrMtrRdngDocERPCrteReqUtilsDvce::getUtilitiesDeviceID)
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreaterequest.UtilitiesDeviceID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getLrn(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreaterequest.SmrtMtrMtrRdngDocERPCrteReqMtrRdngDoc meterReadingDocument) {
            return Optional.ofNullable(meterReadingDocument.getUtiltiesMeasurementTask())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreaterequest.SmrtMtrMtrRdngDocERPCrteReqUtilsMsmtTsk::getUtilitiesMeasurementTaskID)
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreaterequest.UtilitiesMeasurementTaskID::getValue)
                    .filter(categoryCode -> !Checks.is(categoryCode).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getLrn(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreaterequest.SmrtMtrMtrRdngDocERPCrteReqMtrRdngDoc meterReadingDocument) {
            return Optional.ofNullable(meterReadingDocument.getUtiltiesMeasurementTask())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreaterequest.SmrtMtrMtrRdngDocERPCrteReqUtilsMsmtTsk::getUtilitiesMeasurementTaskID)
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreaterequest.UtilitiesMeasurementTaskID::getValue)
                    .filter(categoryCode -> !Checks.is(categoryCode).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getReadingReasonCode(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreaterequest.SmrtMtrMtrRdngDocERPCrteReqMtrRdngDoc meterReadingDocument) {
            return Optional.ofNullable(meterReadingDocument.getMeterReadingReasonCode())
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getReadingReasonCode(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreaterequest.SmrtMtrMtrRdngDocERPCrteReqMtrRdngDoc meterReadingDocument) {
            return Optional.ofNullable(meterReadingDocument.getMeterReadingReasonCode())
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }
    }
}