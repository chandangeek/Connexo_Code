/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.measurementtaskassignment;

import com.energyict.mdc.sap.soap.webservices.impl.ProcessingResultCode;
import com.energyict.mdc.sap.soap.webservices.impl.SeverityCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangeconfirmation.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangeconfirmation.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangeconfirmation.Log;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangeconfirmation.LogItem;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangeconfirmation.ObjectFactory;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangeconfirmation.UtilsTmeSersERPMsmtTskAssgmtChgConfMsg;

import java.time.Instant;
import java.util.UUID;
import java.util.logging.Level;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID;

public class MeasurementTaskAssignmentChangeConfirmationMessage {

    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();
    private UtilsTmeSersERPMsmtTskAssgmtChgConfMsg confirmationMessage;

    public UtilsTmeSersERPMsmtTskAssgmtChgConfMsg getConfirmationMessage() {
        return confirmationMessage;
    }

    public static MeasurementTaskAssignmentChangeConfirmationMessage.Builder builder(Instant now, MeasurementTaskAssignmentChangeRequestMessage message) {
        return new MeasurementTaskAssignmentChangeConfirmationMessage().new Builder(now, message);
    }

    public class Builder {

        private Builder(Instant now, MeasurementTaskAssignmentChangeRequestMessage message) {
            confirmationMessage = OBJECT_FACTORY.createUtilsTmeSersERPMsmtTskAssgmtChgConfMsg();
            confirmationMessage.setMessageHeader(createHeader(now, message.getId(), message.getUuid()));
        }

        public MeasurementTaskAssignmentChangeConfirmationMessage.Builder create() {
            confirmationMessage.setLog(createLog());
            return this;
        }

        public MeasurementTaskAssignmentChangeConfirmationMessage.Builder from(String level, String errorMessage) {
            confirmationMessage.setLog(createLog(getSeverityCode(level), errorMessage));
            return this;
        }

        public MeasurementTaskAssignmentChangeConfirmationMessage build() {
            return MeasurementTaskAssignmentChangeConfirmationMessage.this;
        }

        private BusinessDocumentMessageHeader createHeader(Instant now, String id, String uuid) {
            BusinessDocumentMessageID messageID = OBJECT_FACTORY.createBusinessDocumentMessageID();
            messageID.setValue(id);

            BusinessDocumentMessageHeader messageHeader = OBJECT_FACTORY.createBusinessDocumentMessageHeader();
            messageHeader.setReferenceID(messageID);
            com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangeconfirmation.UUID newUUID = OBJECT_FACTORY.createUUID();
            newUUID.setValue(UUID.randomUUID().toString());
            com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangeconfirmation.UUID referenceUUID = OBJECT_FACTORY.createUUID();
            referenceUUID.setValue(uuid);
            messageHeader.setUUID(newUUID);
            messageHeader.setReferenceUUID(referenceUUID);
            messageHeader.setCreationDateTime(now);
            return messageHeader;
        }

        private String getSeverityCode(String level) {
            if (level.equals(Level.SEVERE.getName())) {
                return SeverityCode.ERROR.getCode();
            } else if (level.equals(Level.WARNING.getName())) {
                return SeverityCode.WARNING.getCode();
            } else {
                return SeverityCode.INFORMATION.getCode();
            }
        }

        private Log createLog() {
            Log log = OBJECT_FACTORY.createLog();
            log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.SUCCESSFUL.getCode());
            return log;
        }

        private Log createLog(String severityCode, String errorMessage) {
            LogItem logItem = OBJECT_FACTORY.createLogItem();

            logItem.setTypeID(UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID);
            logItem.setSeverityCode(severityCode);
            logItem.setNote(errorMessage);

            Log log = OBJECT_FACTORY.createLog();
            log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.FAILED.getCode());
            log.getItem().add(logItem);

            return log;
        }
    }
}
