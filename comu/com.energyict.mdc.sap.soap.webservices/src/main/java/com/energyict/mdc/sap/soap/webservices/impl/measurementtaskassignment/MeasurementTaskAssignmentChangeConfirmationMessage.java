/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.measurementtaskassignment;

import com.energyict.mdc.sap.soap.webservices.impl.ProcessingResultCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangeconfirmation.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangeconfirmation.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangeconfirmation.Log;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangeconfirmation.LogItem;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangeconfirmation.ObjectFactory;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangeconfirmation.UtilsTmeSersERPMsmtTskAssgmtChgConfMsg;

import java.time.Instant;
import java.util.UUID;
import java.util.logging.Level;

public class MeasurementTaskAssignmentChangeConfirmationMessage {

    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();
    private UtilsTmeSersERPMsmtTskAssgmtChgConfMsg confirmationMessage;

    public UtilsTmeSersERPMsmtTskAssgmtChgConfMsg getConfirmationMessage() {
        return confirmationMessage;
    }

    public static MeasurementTaskAssignmentChangeConfirmationMessage.Builder builder(Instant now, String id) {
        return new MeasurementTaskAssignmentChangeConfirmationMessage().new Builder(now, id);
    }

    public class Builder {

        private Builder(Instant now, String id) {
            confirmationMessage = OBJECT_FACTORY.createUtilsTmeSersERPMsmtTskAssgmtChgConfMsg();
            confirmationMessage.setMessageHeader(createHeader(now, id));
        }

        public MeasurementTaskAssignmentChangeConfirmationMessage.Builder create() {
            confirmationMessage.setLog(createLog());
            return this;
        }

        public MeasurementTaskAssignmentChangeConfirmationMessage.Builder from(String level, String typeId, String errorMessage) {
            confirmationMessage.setLog(createLog(getSeverityCode(level), typeId, errorMessage));
            return this;
        }

        public MeasurementTaskAssignmentChangeConfirmationMessage build() {
            return MeasurementTaskAssignmentChangeConfirmationMessage.this;
        }

        private BusinessDocumentMessageHeader createHeader(Instant now, String id) {
            BusinessDocumentMessageID messageID = OBJECT_FACTORY.createBusinessDocumentMessageID();
            messageID.setValue(id);

            BusinessDocumentMessageHeader messageHeader = OBJECT_FACTORY.createBusinessDocumentMessageHeader();
            messageHeader.setReferenceID(messageID);
            com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangeconfirmation.UUID newUUID = OBJECT_FACTORY.createUUID();
            newUUID.setValue(UUID.randomUUID().toString());
            messageHeader.setUUID(newUUID);
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

        private Log createLog(String severityCode, String typeId, String errorMessage) {
            LogItem logItem = OBJECT_FACTORY.createLogItem();

            logItem.setTypeID(typeId);
            logItem.setSeverityCode(severityCode);
            logItem.setNote(errorMessage);

            Log log = OBJECT_FACTORY.createLog();
            log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.FAILED.getCode());
            log.getItem().add(logItem);

            return log;
        }
    }
}
