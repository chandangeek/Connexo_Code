/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.measurementtaskassignment;

import com.elster.jupiter.util.streams.Predicates;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.ProcessingResultCode;
import com.energyict.mdc.sap.soap.webservices.impl.SeverityCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangeconfirmation.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangeconfirmation.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangeconfirmation.Log;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangeconfirmation.LogItem;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangeconfirmation.LogItemCategoryCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangeconfirmation.ObjectFactory;
import com.energyict.mdc.sap.soap.wsdl.webservices.measurementtaskassignmentchangeconfirmation.UtilsTmeSersERPMsmtTskAssgmtChgConfMsg;

import com.google.common.base.Strings;

import java.time.Instant;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.logging.Level;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.PROCESSING_ERROR_CATEGORY_CODE;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.SUCCESSFUL_PROCESSING_TYPE_ID;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID;

public class MeasurementTaskAssignmentChangeConfirmationMessage {

    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();
    private UtilsTmeSersERPMsmtTskAssgmtChgConfMsg confirmationMessage;

    public UtilsTmeSersERPMsmtTskAssgmtChgConfMsg getConfirmationMessage() {
        return confirmationMessage;
    }

    public static MeasurementTaskAssignmentChangeConfirmationMessage.Builder builder(Instant now, String id, String uuid, String meteringSystemId) {
        return new MeasurementTaskAssignmentChangeConfirmationMessage().new Builder(now, id, uuid, meteringSystemId);
    }

    public class Builder {

        private Builder(Instant now, String id, String uuid, String meteringSystemId) {
            confirmationMessage = OBJECT_FACTORY.createUtilsTmeSersERPMsmtTskAssgmtChgConfMsg();
            confirmationMessage.setMessageHeader(createHeader(now, id, uuid, meteringSystemId));
        }

        public MeasurementTaskAssignmentChangeConfirmationMessage.Builder create() {
            confirmationMessage.setLog(createSuccessfulLog());
            return this;
        }

        public MeasurementTaskAssignmentChangeConfirmationMessage.Builder from(Level level, String errorMessage) {
            confirmationMessage.setLog(createFailLog(SeverityCode.getSeverityCode(level), errorMessage));
            return this;
        }

        public MeasurementTaskAssignmentChangeConfirmationMessage build() {
            return MeasurementTaskAssignmentChangeConfirmationMessage.this;
        }

        private BusinessDocumentMessageHeader createHeader(Instant now, String id, String uuid, String meteringSystemId) {
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

            messageHeader.setSenderBusinessSystemID(meteringSystemId);
            messageHeader.setReconciliationIndicator(true);
            messageHeader.setCreationDateTime(now);
            return messageHeader;
        }

        private Log createSuccessfulLog() {
            LogItem logItem = OBJECT_FACTORY.createLogItem();

            logItem.setTypeID(SUCCESSFUL_PROCESSING_TYPE_ID);
            logItem.setSeverityCode(SeverityCode.INFORMATION.getCode());
            logItem.setNote(MessageSeeds.OK_RESULT.getDefaultFormat());

            Log log = OBJECT_FACTORY.createLog();
            log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.SUCCESSFUL.getCode());
            log.getItem().add(logItem);

            setMaximumLogItemSeverityCode(log);

            return log;
        }

        private Log createFailLog(String severityCode, String errorMessage) {
            LogItem logItem = OBJECT_FACTORY.createLogItem();

            logItem.setTypeID(UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID);
            LogItemCategoryCode logItemCategoryCode = OBJECT_FACTORY.createLogItemCategoryCode();
            logItemCategoryCode.setValue(PROCESSING_ERROR_CATEGORY_CODE);
            logItem.setCategoryCode(logItemCategoryCode);
            logItem.setSeverityCode(severityCode);
            logItem.setNote(errorMessage);

            Log log = OBJECT_FACTORY.createLog();
            log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.FAILED.getCode());
            log.getItem().add(logItem);

            setMaximumLogItemSeverityCode(log);

            return log;
        }

        private void setMaximumLogItemSeverityCode(Log log) {
            OptionalInt maxInt = log.getItem().stream().map(LogItem::getSeverityCode)
                    .filter(Predicates.not(Strings::isNullOrEmpty))
                    .mapToInt(Integer::parseInt)
                    .max();
            if (maxInt.isPresent()) {
                Integer value = maxInt.getAsInt();
                log.setMaximumLogItemSeverityCode(value.toString());
            }
        }
    }
}
