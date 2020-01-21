/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.meterreadingdocument;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
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

import com.google.common.base.Strings;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

public class MeterReadingDocumentCreateResultMessage {

    private final ObjectFactory OBJECT_FACTORY = new ObjectFactory();
    private final com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.ObjectFactory BULK_OBJECT_FACTORY =
            new com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.ObjectFactory();

    private MtrRdngDocERPRsltBulkCrteReqMsg bulkResultMessage;
    private MtrRdngDocERPRsltCrteReqMsg resultMessage;
    private boolean bulk;

    //statistic
    private int documentsTotal;
    private int documentsCancelledBySap;
    private int documentsSuccessfullyProcessed;

    public MtrRdngDocERPRsltBulkCrteReqMsg getBulkResultMessage() {
        return bulkResultMessage;
    }

    public MtrRdngDocERPRsltCrteReqMsg getResultMessage() {
        return resultMessage;
    }

    public boolean isBulk() {
        return bulk;
    }

    public int getDocumentsTotal() {
        return documentsTotal;
    }

    public int getDocumentsCancelledBySap() {
        return documentsCancelledBySap;
    }

    public int getDocumentsSuccessfullyProcessed() {
        return documentsSuccessfullyProcessed;
    }

    public static Builder builder() {
        return new MeterReadingDocumentCreateResultMessage().new Builder();
    }

    public class Builder {

        private Builder() {
            resultMessage = OBJECT_FACTORY.createMtrRdngDocERPRsltCrteReqMsg();
            bulkResultMessage = BULK_OBJECT_FACTORY.createMtrRdngDocERPRsltBulkCrteReqMsg();
        }

        public Builder from(ServiceCall parent, List<ServiceCall> children, Instant now, String meteringSystemId) {
            documentsTotal = children.size();
            MasterMeterReadingDocumentCreateResultDomainExtension extension = parent.getExtensionFor(new MasterMeterReadingDocumentCreateResultCustomPropertySet()).get();
            MeterReadingDocumentCreateResultMessage.this.bulk = extension.isBulk();

            if (bulk) {
                bulkResultMessage.setMessageHeader(createBulkHeader(extension, now, meteringSystemId));
                children.forEach(child -> {
                    MeterReadingDocumentCreateResultDomainExtension childExtension = child.getExtensionFor(new MeterReadingDocumentCreateResultCustomPropertySet()).get();
                    if (!childExtension.isCancelledBySap()) {
                        if (childExtension.getReading() == null) {
                            child.log(LogLevel.FINEST, "No readings to send.");
                            if (!child.getState().equals(DefaultState.CANCELLED)) {
                                child.requestTransition(DefaultState.ONGOING);
                                child.requestTransition(DefaultState.FAILED);
                            }
                        } else {
                            com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.MtrRdngDocERPRsltCrteReqMsg crteReqMsg = BULK_OBJECT_FACTORY.createMtrRdngDocERPRsltCrteReqMsg();
                            crteReqMsg.setMessageHeader(createBulkItemHeader(childExtension, now, meteringSystemId));
                            crteReqMsg.setMeterReadingDocument(createBulkBody(child));
                            bulkResultMessage.getMeterReadingDocumentERPResultCreateRequestMessage().add(crteReqMsg);
                        }
                    } else {
                        documentsCancelledBySap++;
                    }
                });

                if (bulkResultMessage.getMeterReadingDocumentERPResultCreateRequestMessage().isEmpty()) {
                    parent.log(LogLevel.FINEST, "Do not send response. No readings to send.");
                    parent.requestTransition(DefaultState.FAILED);
                }
            } else {
                resultMessage.setMessageHeader(createHeader(extension, now, meteringSystemId));
                if (!children.isEmpty()) {
                    ServiceCall chldSrvCall = children.get(0);
                    MeterReadingDocumentCreateResultDomainExtension chldExtension = chldSrvCall.getExtensionFor(new MeterReadingDocumentCreateResultCustomPropertySet()).get();
                    if (!chldExtension.isCancelledBySap()) {
                        if (chldExtension.getReading() == null) {
                            chldSrvCall.log(LogLevel.FINEST, "No readings to send.");
                            if (!chldSrvCall.getState().equals(DefaultState.CANCELLED)) {
                                chldSrvCall.requestTransition(DefaultState.ONGOING);
                                chldSrvCall.requestTransition(DefaultState.FAILED);
                                resultMessage.setMeterReadingDocument(null);
                                parent.log(LogLevel.FINEST, "Do not send response. No readings to send.");
                                parent.requestTransition(DefaultState.FAILED);
                            }
                        } else {
                            resultMessage.setMeterReadingDocument(createBody(chldSrvCall));
                        }
                    }
                }
            }
            return this;
        }

        private BusinessDocumentMessageHeader createHeader(MasterMeterReadingDocumentCreateResultDomainExtension extension, Instant now, String meteringSystemId) {

            UUID uuid = OBJECT_FACTORY.createUUID();
            uuid.setValue(extension.getRequestUUID());

            BusinessDocumentMessageHeader messageHeader = OBJECT_FACTORY.createBusinessDocumentMessageHeader();
            if (!Strings.isNullOrEmpty(extension.getReferenceID())) {
                BusinessDocumentMessageID id = OBJECT_FACTORY.createBusinessDocumentMessageID();
                id.setValue(extension.getReferenceID());
                messageHeader.setReferenceID(id);
            }

            messageHeader.setUUID(uuid);
            if (!Strings.isNullOrEmpty(extension.getReferenceUuid())) {
                UUID referenceUuid = OBJECT_FACTORY.createUUID();
                referenceUuid.setValue(extension.getReferenceUuid());
                messageHeader.setReferenceUUID(referenceUuid);
            }

            messageHeader.setCreationDateTime(now);

            messageHeader.setSenderBusinessSystemID(meteringSystemId);
            messageHeader.setReconciliationIndicator(true);

            return messageHeader;
        }

        private com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.BusinessDocumentMessageHeader createBulkHeader(MasterMeterReadingDocumentCreateResultDomainExtension extension, Instant now, String meteringSystemId) {
            com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.UUID uuid =
                    BULK_OBJECT_FACTORY.createUUID();
            uuid.setValue(extension.getRequestUUID());

            com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.BusinessDocumentMessageHeader messageHeader = BULK_OBJECT_FACTORY.createBusinessDocumentMessageHeader();
            if (!Strings.isNullOrEmpty(extension.getReferenceID())) {
                com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.BusinessDocumentMessageID id =
                        BULK_OBJECT_FACTORY.createBusinessDocumentMessageID();
                id.setValue(extension.getReferenceID());
                messageHeader.setReferenceID(id);
            }
            messageHeader.setUUID(uuid);

            if (!Strings.isNullOrEmpty(extension.getReferenceUuid())) {
                com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.UUID referenceUuid =
                        BULK_OBJECT_FACTORY.createUUID();
                referenceUuid.setValue(extension.getReferenceUuid());
                messageHeader.setReferenceUUID(referenceUuid);
            }

            messageHeader.setSenderBusinessSystemID(meteringSystemId);
            messageHeader.setReconciliationIndicator(true);

            messageHeader.setCreationDateTime(now);
            return messageHeader;
        }


        private com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.BusinessDocumentMessageHeader createBulkItemHeader(MeterReadingDocumentCreateResultDomainExtension extension, Instant now, String meteringSystemId) {
            com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.UUID uuid =
                    BULK_OBJECT_FACTORY.createUUID();
            uuid.setValue(java.util.UUID.randomUUID().toString());

            com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.BusinessDocumentMessageHeader messageHeader = BULK_OBJECT_FACTORY.createBusinessDocumentMessageHeader();
            if (!Strings.isNullOrEmpty(extension.getReferenceID())) {
                com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.BusinessDocumentMessageID id =
                        BULK_OBJECT_FACTORY.createBusinessDocumentMessageID();
                id.setValue(extension.getReferenceID());
                messageHeader.setReferenceID(id);
            }
            messageHeader.setUUID(uuid);

            if (!Strings.isNullOrEmpty(extension.getReferenceUuid())) {
                com.energyict.mdc.sap.soap.wsdl.webservices.meterreadingresultbulkcreaterequest.UUID referenceUuid =
                        BULK_OBJECT_FACTORY.createUUID();
                referenceUuid.setValue(extension.getReferenceUuid());
                messageHeader.setReferenceUUID(referenceUuid);
            }

            messageHeader.setSenderBusinessSystemID(meteringSystemId);
            messageHeader.setReconciliationIndicator(true);

            messageHeader.setCreationDateTime(now);
            return messageHeader;
        }


        private MtrRdngDocERPRsltCrteReqMtrRdngDoc createBody(ServiceCall child) {
            MeterReadingDocumentCreateResultDomainExtension childExtension = child.getExtensionFor(new MeterReadingDocumentCreateResultCustomPropertySet()).get();

            MtrRdngDocERPRsltCrteReqMtrRdngDoc meterReadingDocument = OBJECT_FACTORY.createMtrRdngDocERPRsltCrteReqMtrRdngDoc();
            MeterReadingDocumentID meterReadingDocumentID = OBJECT_FACTORY.createMeterReadingDocumentID();
            meterReadingDocumentID.setValue(childExtension.getMeterReadingDocumentId());
            meterReadingDocument.setID(meterReadingDocumentID);
            meterReadingDocument.setMeterReadingReasonCode(childExtension.getReadingReasonCode());
            meterReadingDocument.setScheduledMeterReadingDate(childExtension.getRequestedScheduledReadingDate());

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
                documentsSuccessfullyProcessed++;
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
            meterReadingDocument.setScheduledMeterReadingDate(childExtension.getRequestedScheduledReadingDate());

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
                documentsSuccessfullyProcessed++;
            }

            return meterReadingDocument;
        }

        public MeterReadingDocumentCreateResultMessage build() {
            return MeterReadingDocumentCreateResultMessage.this;
        }
    }
}