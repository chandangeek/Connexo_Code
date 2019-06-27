/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MasterMeterReadingDocumentCreateResultCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MasterMeterReadingDocumentCreateResultDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MeterReadingDocumentCreateResultCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreadingdocument.MeterReadingDocumentCreateResultDomainExtension;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.MtrRdngDocERPRsltBulkCrteReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreaterequest.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreaterequest.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreaterequest.MeterReadingDocumentID;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreaterequest.MtrRdngDocERPRsltCrteReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreaterequest.MtrRdngDocERPRsltCrteReqMtrRdngDoc;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreaterequest.MtrRdngDocERPRsltCrteReqRslt;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreaterequest.MtrRdngDocERPRsltCrteReqUtilsDvce;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreaterequest.MtrRdngDocERPRsltCrteReqUtilsMsmtTsk;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreaterequest.ObjectFactory;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreaterequest.UUID;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreaterequest.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultcreaterequest.UtilitiesMeasurementTaskID;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

public class MeterReadingDocumentCreateResultMessage {

    private final ObjectFactory OBJECT_FACTORY = new ObjectFactory();
    private final com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.ObjectFactory BULK_OBJECT_FACTORY =
            new com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.ObjectFactory();

    private MtrRdngDocERPRsltBulkCrteReqMsg bulkResultMessage;
    private MtrRdngDocERPRsltCrteReqMsg resultMessage;
    private String url;
    private boolean bulk;

    public MtrRdngDocERPRsltBulkCrteReqMsg getBulkResultMessage() {
        return bulkResultMessage;
    }

    public MtrRdngDocERPRsltCrteReqMsg getResultMessage() {
        return resultMessage;
    }

    public String getUrl() {
        return url;
    }

    public boolean isBulk() {
        return bulk;
    }

    public static Builder builder() {
        return new MeterReadingDocumentCreateResultMessage().new Builder();
    }

    public class Builder {

        private Builder() {
            resultMessage = OBJECT_FACTORY.createMtrRdngDocERPRsltCrteReqMsg();
            bulkResultMessage = BULK_OBJECT_FACTORY.createMtrRdngDocERPRsltBulkCrteReqMsg();
        }

        public Builder from(ServiceCall parent, List<ServiceCall> children) {
            MasterMeterReadingDocumentCreateResultDomainExtension extension = parent.getExtensionFor(new MasterMeterReadingDocumentCreateResultCustomPropertySet()).get();
            MeterReadingDocumentCreateResultMessage.this.bulk = extension.isBulk();

            if (bulk) {
                bulkResultMessage.setMessageHeader(createBulkHeader(extension));
                children.forEach(child -> {
                    com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.MtrRdngDocERPRsltCrteReqMsg crteReqMsg = BULK_OBJECT_FACTORY.createMtrRdngDocERPRsltCrteReqMsg();
                    crteReqMsg.setMessageHeader(createBulkHeader(extension));
                    crteReqMsg.setMeterReadingDocument(createBulkBody(child));
                    bulkResultMessage.getMeterReadingDocumentERPResultCreateRequestMessage().add(crteReqMsg);
                });
            } else {
                resultMessage.setMessageHeader(createHeader(extension));
                if (!children.isEmpty()) {
                    resultMessage.setMeterReadingDocument(createBody(children.get(0)));
                }
            }
            return this;
        }

        private BusinessDocumentMessageHeader createHeader(MasterMeterReadingDocumentCreateResultDomainExtension extension) {
            BusinessDocumentMessageID id = OBJECT_FACTORY.createBusinessDocumentMessageID();
            id.setValue(extension.getReferenceID());

            UUID uuid = OBJECT_FACTORY.createUUID();
            uuid.setValue(extension.getRequestUUID());

            BusinessDocumentMessageHeader messageHeader = OBJECT_FACTORY.createBusinessDocumentMessageHeader();
            messageHeader.setReferenceID(id);
            messageHeader.setUUID(uuid);

            return messageHeader;
        }

        private com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.BusinessDocumentMessageHeader createBulkHeader(MasterMeterReadingDocumentCreateResultDomainExtension extension) {
            com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.BusinessDocumentMessageID id =
                    BULK_OBJECT_FACTORY.createBusinessDocumentMessageID();
            id.setValue(extension.getReferenceID());

            com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.UUID uuid =
                    BULK_OBJECT_FACTORY.createUUID();
            uuid.setValue(extension.getRequestUUID());

            com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.BusinessDocumentMessageHeader messageHeader = BULK_OBJECT_FACTORY.createBusinessDocumentMessageHeader();
            messageHeader.setReferenceID(id);
            messageHeader.setUUID(uuid);
            return messageHeader;
        }

        private MtrRdngDocERPRsltCrteReqMtrRdngDoc createBody(ServiceCall child) {
            MeterReadingDocumentCreateResultDomainExtension childExtension = child.getExtensionFor(new MeterReadingDocumentCreateResultCustomPropertySet()).get();

            MtrRdngDocERPRsltCrteReqMtrRdngDoc meterReadingDocument = OBJECT_FACTORY.createMtrRdngDocERPRsltCrteReqMtrRdngDoc();
            MeterReadingDocumentID meterReadingDocumentID = OBJECT_FACTORY.createMeterReadingDocumentID();
            meterReadingDocumentID.setValue(childExtension.getMeterReadingDocumentId());
            meterReadingDocument.setID(meterReadingDocumentID);
            meterReadingDocument.setMeterReadingReasonCode(childExtension.getReadingReasonCode());
            meterReadingDocument.setScheduledMeterReadingDate(childExtension.getScheduledReadingDate());

            MtrRdngDocERPRsltCrteReqUtilsMsmtTsk mtrRdngDocERPRsltCrteReqUtilsMsmtTsk = OBJECT_FACTORY.createMtrRdngDocERPRsltCrteReqUtilsMsmtTsk();
            MtrRdngDocERPRsltCrteReqUtilsDvce mtrRdngDocERPRsltCrteReqUtilsDvce = OBJECT_FACTORY.createMtrRdngDocERPRsltCrteReqUtilsDvce();
            UtilitiesDeviceID utilitiesDeviceID = OBJECT_FACTORY.createUtilitiesDeviceID();
            utilitiesDeviceID.setValue(childExtension.getDeviceId());
            mtrRdngDocERPRsltCrteReqUtilsDvce.setUtilitiesDeviceID(utilitiesDeviceID);
            mtrRdngDocERPRsltCrteReqUtilsMsmtTsk.setUtiltiesDevice(mtrRdngDocERPRsltCrteReqUtilsDvce);

            UtilitiesMeasurementTaskID utilitiesMeasurementTaskID = OBJECT_FACTORY.createUtilitiesMeasurementTaskID();
            utilitiesMeasurementTaskID.setValue(childExtension.getLrn());
            mtrRdngDocERPRsltCrteReqUtilsMsmtTsk.setUtilitiesMeasurementTaskID(utilitiesMeasurementTaskID);
            meterReadingDocument.setUtiltiesMeasurementTask(mtrRdngDocERPRsltCrteReqUtilsMsmtTsk);

            if (childExtension.getReading() != null) {
                MtrRdngDocERPRsltCrteReqRslt result = OBJECT_FACTORY.createMtrRdngDocERPRsltCrteReqRslt();
                result.setMeterReadingResultValue(childExtension.getReading());
                result.setActualMeterReadingDate(childExtension.getActualReadingDate());
                result.setActualMeterReadingTime(LocalDateTime.ofInstant(childExtension.getActualReadingDate(), ZoneId.systemDefault()).toLocalTime());
                meterReadingDocument.setResult(result);
            }

            return meterReadingDocument;
        }

        private com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.MtrRdngDocERPRsltCrteReqMtrRdngDoc createBulkBody(ServiceCall child) {
            MeterReadingDocumentCreateResultDomainExtension childExtension = child.getExtensionFor(new MeterReadingDocumentCreateResultCustomPropertySet()).get();

            com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.MtrRdngDocERPRsltCrteReqMtrRdngDoc meterReadingDocument = BULK_OBJECT_FACTORY.createMtrRdngDocERPRsltCrteReqMtrRdngDoc();
            com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.MeterReadingDocumentID meterReadingDocumentID = BULK_OBJECT_FACTORY.createMeterReadingDocumentID();
            meterReadingDocumentID.setValue(childExtension.getMeterReadingDocumentId());
            meterReadingDocument.setID(meterReadingDocumentID);
            meterReadingDocument.setMeterReadingReasonCode(childExtension.getReadingReasonCode());
            meterReadingDocument.setScheduledMeterReadingDate(childExtension.getScheduledReadingDate());

            com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.UtilitiesDeviceID utilitiesDeviceID = BULK_OBJECT_FACTORY.createUtilitiesDeviceID();
            utilitiesDeviceID.setValue(childExtension.getDeviceId().toString());
            com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.UtilitiesMeasurementTaskID utilitiesMeasurementTaskID = BULK_OBJECT_FACTORY.createUtilitiesMeasurementTaskID();
            utilitiesMeasurementTaskID.setValue(childExtension.getLrn().toString());

            com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.MtrRdngDocERPRsltCrteReqUtilsDvce mtrRdngDocERPRsltCrteReqUtilsDvce = BULK_OBJECT_FACTORY.createMtrRdngDocERPRsltCrteReqUtilsDvce();
            mtrRdngDocERPRsltCrteReqUtilsDvce.setUtilitiesDeviceID(utilitiesDeviceID);

            com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.MtrRdngDocERPRsltCrteReqUtilsMsmtTsk mtrRdngDocERPRsltCrteReqUtilsMsmtTsk = BULK_OBJECT_FACTORY.createMtrRdngDocERPRsltCrteReqUtilsMsmtTsk();
            mtrRdngDocERPRsltCrteReqUtilsMsmtTsk.setUtiltiesDevice(mtrRdngDocERPRsltCrteReqUtilsDvce);
            mtrRdngDocERPRsltCrteReqUtilsMsmtTsk.setUtilitiesMeasurementTaskID(utilitiesMeasurementTaskID);
            meterReadingDocument.setUtiltiesMeasurementTask(mtrRdngDocERPRsltCrteReqUtilsMsmtTsk);

            if (childExtension.getReading() != null) {
                com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.MtrRdngDocERPRsltCrteReqRslt result =
                        BULK_OBJECT_FACTORY.createMtrRdngDocERPRsltCrteReqRslt();
                result.setMeterReadingResultValue(childExtension.getReading());
                result.setActualMeterReadingDate(childExtension.getActualReadingDate());
                result.setActualMeterReadingTime(LocalDateTime.ofInstant(childExtension.getActualReadingDate(), ZoneId.systemDefault()).toLocalTime());
                meterReadingDocument.setResult(result);
            }

            return meterReadingDocument;
        }

        public MeterReadingDocumentCreateResultMessage build() {
            return MeterReadingDocumentCreateResultMessage.this;
        }
    }
}