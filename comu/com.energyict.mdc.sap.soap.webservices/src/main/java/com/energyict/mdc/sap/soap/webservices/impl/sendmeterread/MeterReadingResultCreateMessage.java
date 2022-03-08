/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.sendmeterread;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.sap.soap.webservices.impl.AbstractSapMessage;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingresultcreaterequest.SmrtMtrMtrRdngDocERPRsltCrteReqMtrRdngDoc;

import java.util.Optional;

public class MeterReadingResultCreateMessage extends AbstractSapMessage {

    private static final String ID_XML_NAME = "MeterReadingDocument.ID";
    private static final String DEVICE_ID_XML_NAME = "UtilitiesDevice.UtilitiesDeviceID";
    private static final String LRN_XML_NAME = "UtilitiesMeasurementTaskID.UtilitiesDeviceID";
    private static final String READING_REASON_CODE_XML_NAME = "MeterReadingReasonCode";

    private String id;
    private String deviceId;
    private String lrn;
    private String readingReasonCode;
    private MeterReadingResultMessage meterReadingResultMessage;

    private MeterReadingResultCreateMessage() {
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

    public MeterReadingResultMessage getMeterReadingResultMessage() {
        return meterReadingResultMessage;
    }

    static MeterReadingResultCreateMessage.Builder builder() {
        return new MeterReadingResultCreateMessage().new Builder();
    }

    public class Builder {

        private Builder() {
        }

        public MeterReadingResultCreateMessage.Builder from(SmrtMtrMtrRdngDocERPRsltCrteReqMtrRdngDoc requestMessage) {
            Optional.ofNullable(requestMessage)
                    .ifPresent(singleRequestMessage -> {
                        setValues(getId(singleRequestMessage), getDeviceId(singleRequestMessage),
                                getLrn(singleRequestMessage), getReadingReasonCode(singleRequestMessage));
                    });
            MeterReadingResultMessage resultMessage = MeterReadingResultMessage
                    .builder()
                    .from(requestMessage.getResult())
                    .build();
            meterReadingResultMessage = resultMessage;
            addMissingFields(resultMessage.getMissingFieldsSet());
            return this;
        }

        public Builder setId(String id) {
            MeterReadingResultCreateMessage.this.id = id;
            return this;
        }


        public Builder setDeviceId(String deviceId) {
            MeterReadingResultCreateMessage.this.deviceId = deviceId;
            return this;
        }

        public Builder setLrn(String lrn) {
            MeterReadingResultCreateMessage.this.lrn = lrn;
            return this;
        }

        public Builder setReadingReasonCode(String readingReasonCode) {
            MeterReadingResultCreateMessage.this.readingReasonCode = readingReasonCode;
            return this;
        }

        public MeterReadingResultCreateMessage build() {
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
            return MeterReadingResultCreateMessage.this;
        }

        void setValues(String id, String deviceId, String lrn, String code) {
            setId(id);
            setDeviceId(deviceId);
            setLrn(lrn);
            setReadingReasonCode(code);
        }


        private String getId(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingresultcreaterequest.SmrtMtrMtrRdngDocERPRsltCrteReqMtrRdngDoc meterReadingDocument) {
            return Optional.ofNullable(meterReadingDocument.getID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingresultcreaterequest.MeterReadingDocumentID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }


        private String getDeviceId(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingresultcreaterequest.SmrtMtrMtrRdngDocERPRsltCrteReqMtrRdngDoc meterReadingDocument) {
            return Optional.ofNullable(meterReadingDocument.getUtilitiesMeasurementTask())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingresultcreaterequest.SmrtMtrMtrRdngDocERPRsltCrteReqUtilsMsmtTsk::getUtilitiesDevice)
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingresultcreaterequest.SmrtMtrMtrRdngDocERPRsltCrteReqUtilsDvce::getUtilitiesDeviceID)
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingresultcreaterequest.UtilitiesDeviceID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }


        private String getLrn(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingresultcreaterequest.SmrtMtrMtrRdngDocERPRsltCrteReqMtrRdngDoc meterReadingDocument) {
            return Optional.ofNullable(meterReadingDocument.getUtilitiesMeasurementTask())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingresultcreaterequest.SmrtMtrMtrRdngDocERPRsltCrteReqUtilsMsmtTsk::getUtilitiesMeasurementTaskID)
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingresultcreaterequest.UtilitiesMeasurementTaskID::getValue)
                    .filter(categoryCode -> !Checks.is(categoryCode).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }


        private String getReadingReasonCode(com.energyict.mdc.sap.soap.wsdl.webservices.smartmetermeterreadingresultcreaterequest.SmrtMtrMtrRdngDocERPRsltCrteReqMtrRdngDoc meterReadingDocument) {
            return Optional.ofNullable(meterReadingDocument.getMeterReadingReasonCode())
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

    }
}