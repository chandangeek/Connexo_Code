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

    public static MeasurementTaskAssignmentChangeConfirmationMessage.Builder builder(Instant now, String id, String uuid) {
        return new MeasurementTaskAssignmentChangeConfirmationMessage().new Builder(now, id, uuid);
    }

    public class Builder {

        private Builder(Instant now, String id, String uuid) {
            confirmationMessage = OBJECT_FACTORY.createUtilsTmeSersERPMsmtTskAssgmtChgConfMsg();
            confirmationMessage.setMessageHeader(createHeader(now, id, uuid));
        }

        public MeasurementTaskAssignmentChangeConfirmationMessage.Builder create() {
            confirmationMessage.setLog(createLog());
            return this;
        }

        public MeasurementTaskAssignmentChangeConfirmationMessage.Builder from(Level level, String errorMessage) {
            confirmationMessage.setLog(createLog(SeverityCode.getSeverityCode(level), errorMessage));
            return this;
        }

        public MeasurementTaskAssignmentChangeConfirmationMessage build() {
            return MeasurementTaskAssignmentChangeConfirmationMessage.this;
        }

        private BusinessDocumentMessageHeader createHeader(Instant now, String id, String uuid) {
            BusinessDocumentMessageHeader messageHeader = OBJECT_FACTORY.createBusinessDocumentMessageHeader();
            com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangeconfirmation.UUID newUUID = OBJECT_FACTORY.createUUID();
            newUUID.setValue(UUID.randomUUID().toString());
            messageHeader.setUUID(newUUID);
            if (id != null) {
                BusinessDocumentMessageID messageID = OBJECT_FACTORY.createBusinessDocumentMessageID();
                messageID.setValue(id);
                messageHeader.setReferenceID(messageID);
            }
            if (uuid != null) {
                com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangeconfirmation.UUID referenceUUID = OBJECT_FACTORY.createUUID();
                referenceUUID.setValue(uuid);
                messageHeader.setReferenceUUID(referenceUUID);
            }
            messageHeader.setCreationDateTime(now);
            return messageHeader;
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
