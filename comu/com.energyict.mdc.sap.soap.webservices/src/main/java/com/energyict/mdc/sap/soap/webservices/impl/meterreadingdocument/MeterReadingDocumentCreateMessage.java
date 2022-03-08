/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.sap.soap.webservices.impl.AbstractSapMessage;

import java.time.Instant;
import java.util.Optional;

public class MeterReadingDocumentCreateMessage extends AbstractSapMessage {

    private static final String ID_XML_NAME = "MeterReadingDocument.ID";
    private static final String DEVICE_ID_XML_NAME = "UtilitiesDevice.UtilitiesDeviceID";
    private static final String LRN_XML_NAME = "UtilitiesMeasurementTaskID.UtilitiesDeviceID";
    private static final String READING_REASON_CODE_XML_NAME = "MeterReadingReasonCode";
    private static final String SCHEDULED_DATE_XML_NAME = "ScheduledMeterReadingDate";


    /* headerId headerUUID used only for bulk request */
    private String headerId;
    private String headerUUID;

    private String id;
    private String deviceId;
    private String lrn;
    private String readingReasonCode;
    private Instant scheduledMeterReadingDate;
    private String dataSourceTypeCode;

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

    public String getDataSourceTypeCode() {
        return dataSourceTypeCode;
    }

    public String getHeaderId() {
        return headerId;
    }

    public String getHeaderUUID() {
        return headerUUID;
    }

    static MeterReadingDocumentCreateMessage.Builder builder() {
        return new MeterReadingDocumentCreateMessage().new Builder();
    }

    public class Builder {

        private Builder() {
        }

        public MeterReadingDocumentCreateMessage.Builder from(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreaterequest.SmrtMtrMtrRdngDocERPCrteReqMsg requestMessage) {
            Optional.ofNullable(requestMessage)
                    .ifPresent(bulkRequestMessage -> setValues(getId(bulkRequestMessage.getMeterReadingDocument()),
                            getDeviceId(bulkRequestMessage.getMeterReadingDocument()), getLrn(bulkRequestMessage.getMeterReadingDocument()),
                            getReadingReasonCode(bulkRequestMessage.getMeterReadingDocument()), getDataSourceTypeCode(bulkRequestMessage.getMeterReadingDocument()), bulkRequestMessage.getMeterReadingDocument()
                                    .getScheduledMeterReadingDate(),
                            getHeaderId(bulkRequestMessage.getMessageHeader()),
                            getHeaderUUID(bulkRequestMessage.getMessageHeader())));
            return this;
        }

        public MeterReadingDocumentCreateMessage.Builder from(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreaterequest.SmrtMtrMtrRdngDocERPCrteReqMtrRdngDoc requestMessage) {
            Optional.ofNullable(requestMessage)
                    .ifPresent(singleRequestMessage -> {
                        setValues(getId(singleRequestMessage), getDeviceId(singleRequestMessage),
                                getLrn(singleRequestMessage), getReadingReasonCode(singleRequestMessage), getDataSourceTypeCode(singleRequestMessage),
                                singleRequestMessage.getScheduledMeterReadingDate());
                    });
            return this;
        }

        public Builder setId(String id) {
            MeterReadingDocumentCreateMessage.this.id = id;
            return this;
        }

        public Builder setHeaderId(String headerId) {
            MeterReadingDocumentCreateMessage.this.headerId = headerId;
            return this;
        }

        public Builder setHeaderUUID(String headerUUID) {
            MeterReadingDocumentCreateMessage.this.headerUUID = headerUUID;
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

        public Builder setDataSourceTypeCode(String dataSourceTypeCode) {
            MeterReadingDocumentCreateMessage.this.dataSourceTypeCode = dataSourceTypeCode;
            return this;
        }


        public MeterReadingDocumentCreateMessage build() {
            if (id == null) {
                addMissingField(ID_XML_NAME);
            }
            if (deviceId == null) {
                addMissingField(DEVICE_ID_XML_NAME);
            }
            if (lrn == null) {
                addMissingField(LRN_XML_NAME);
            }
            if (readingReasonCode == null) {
                addMissingField(READING_REASON_CODE_XML_NAME);
            }
            if (scheduledMeterReadingDate == null) {
                addMissingField(SCHEDULED_DATE_XML_NAME);
            }
            return MeterReadingDocumentCreateMessage.this;
        }

        void setValues(String id, String deviceId, String lrn, String code, String dataSourceTypeCode,
                       Instant date) {
            setId(id);
            setDeviceId(deviceId);
            setLrn(lrn);
            setReadingReasonCode(code);
            setDataSourceTypeCode(dataSourceTypeCode);
            setScheduledMeterReadingDate(date);
        }

        void setValues(String id, String deviceId, String lrn, String code, String dataSourceTypeCode,
                       Instant date, String headerId, String headerUUID) {
            setId(id);
            setDeviceId(deviceId);
            setLrn(lrn);
            setReadingReasonCode(code);
            setDataSourceTypeCode(dataSourceTypeCode);
            setScheduledMeterReadingDate(date);
            setHeaderId(headerId);
            setHeaderUUID(headerUUID);
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

        private String getDataSourceTypeCode(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingcreaterequest.SmrtMtrMtrRdngDocERPCrteReqMtrRdngDoc meterReadingDocument) {
            return Optional.ofNullable(meterReadingDocument.getUtilitiesAdvancedMeteringDataSourceTypeCode())
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getDataSourceTypeCode(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreaterequest.SmrtMtrMtrRdngDocERPCrteReqMtrRdngDoc meterReadingDocument) {
            return Optional.ofNullable(meterReadingDocument.getUtilitiesAdvancedMeteringDataSourceTypeCode())
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getHeaderId(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreaterequest.BusinessDocumentMessageHeader header) {
            return Optional.ofNullable(header.getID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreaterequest.BusinessDocumentMessageID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getHeaderUUID(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreaterequest.BusinessDocumentMessageHeader header) {
            return Optional.ofNullable(header.getUUID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingbulkcreaterequest.UUID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

    }
}