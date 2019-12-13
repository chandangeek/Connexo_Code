/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.util.streams.Predicates;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.ProcessingResultCode;
import com.energyict.mdc.sap.soap.webservices.impl.SeverityCode;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.UtilitiesDeviceCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.UtilitiesDeviceCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreateconfirmation.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreateconfirmation.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreateconfirmation.Log;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreateconfirmation.LogItem;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreateconfirmation.LogItemCategoryCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreateconfirmation.ObjectFactory;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreateconfirmation.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreateconfirmation.UtilsDvceERPSmrtMtrBlkCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreateconfirmation.UtilsDvceERPSmrtMtrCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreateconfirmation.UtilsDvceERPSmrtMtrCrteConfUtilsDvce;

import com.google.common.base.Strings;

import java.time.Instant;
import java.util.List;
import java.util.OptionalInt;
import java.util.UUID;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.PROCESSING_ERROR_CATEGORY_CODE;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.SUCCESSFUL_PROCESSING_TYPE_ID;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID;

public class CreateBulkConfirmationMessageFactory {
    private static final ObjectFactory objectFactory = new ObjectFactory();

    public CreateBulkConfirmationMessageFactory() {
    }

    public UtilsDvceERPSmrtMtrBlkCrteConfMsg createMessage(ServiceCall parent, List<ServiceCall> children, Instant now) {
        MasterUtilitiesDeviceCreateRequestDomainExtension extension = parent.getExtensionFor(new MasterUtilitiesDeviceCreateRequestCustomPropertySet()).get();

        UtilsDvceERPSmrtMtrBlkCrteConfMsg confirmationMessage = objectFactory.createUtilsDvceERPSmrtMtrBlkCrteConfMsg();
        confirmationMessage.setMessageHeader(createMessageHeader(extension.getRequestID(), extension.getUuid(), now));

        switch (parent.getState()) {
            case CANCELLED:
                confirmationMessage.setLog(createFailedLog(MessageSeeds.SERVICE_CALL_WAS_CANCELLED.getDefaultFormat(null)));
                break;
            case SUCCESSFUL:
                confirmationMessage.setLog(createSuccessfulLog());
                break;
            case FAILED:
                confirmationMessage.setLog(createFailedLog(MessageSeeds.BULK_REQUEST_WAS_FAILED.getDefaultFormat(null)));
                break;
            case PARTIAL_SUCCESS:
                confirmationMessage.setLog(createPartiallySuccessfulLog());
                break;
            default:
                // No specific action required for these states
                break;
        }
        createBody(confirmationMessage, children, now);

        return confirmationMessage;
    }

    public UtilsDvceERPSmrtMtrBlkCrteConfMsg createMessage(UtilitiesDeviceCreateRequestMessage message, MessageSeeds messageSeed, Instant now) {
        UtilsDvceERPSmrtMtrBlkCrteConfMsg confirmationMessage = objectFactory.createUtilsDvceERPSmrtMtrBlkCrteConfMsg();
        confirmationMessage.setMessageHeader(createMessageHeader(message.getRequestID(), message.getUuid(), now));
        message.getUtilitiesDeviceCreateMessages()
                .forEach(item -> {
                    confirmationMessage
                            .getUtilitiesDeviceERPSmartMeterCreateConfirmationMessage()
                            .add(createFailedChildMessage(item, now));

                });
        confirmationMessage.setLog(createFailedLog(messageSeed.getDefaultFormat(null)));
        return confirmationMessage;
    }

    private void createBody(UtilsDvceERPSmrtMtrBlkCrteConfMsg confirmationMessage,
                            List<ServiceCall> children, Instant now) {

        children.forEach(child -> {
            confirmationMessage.getUtilitiesDeviceERPSmartMeterCreateConfirmationMessage()
                    .add(createChildMessage(child, now));
        });
    }

    private UtilsDvceERPSmrtMtrCrteConfMsg createChildMessage(ServiceCall childServiceCall, Instant now) {
        UtilitiesDeviceCreateRequestDomainExtension extension = childServiceCall.getExtensionFor(new UtilitiesDeviceCreateRequestCustomPropertySet()).get();

        UtilsDvceERPSmrtMtrCrteConfMsg confirmationMessage = objectFactory.createUtilsDvceERPSmrtMtrCrteConfMsg();
        confirmationMessage.setMessageHeader(createChildHeader(now));
        confirmationMessage.setUtilitiesDevice(createChildBody(extension.getDeviceId()));
        if (childServiceCall.getState() == DefaultState.SUCCESSFUL) {
            confirmationMessage.setLog(createSuccessfulLog());
        } else if (childServiceCall.getState() == DefaultState.FAILED || childServiceCall.getState() == DefaultState.CANCELLED) {
            confirmationMessage.setLog(createFailedLog(extension.getErrorMessage()));
        }
        return confirmationMessage;
    }

    private UtilsDvceERPSmrtMtrCrteConfMsg createFailedChildMessage(UtilitiesDeviceCreateMessage message, Instant now) {

        UtilsDvceERPSmrtMtrCrteConfMsg confirmationMessage = objectFactory.createUtilsDvceERPSmrtMtrCrteConfMsg();
        confirmationMessage.setMessageHeader(createChildHeader(now));
        confirmationMessage.setUtilitiesDevice(createChildBody(message.getDeviceId()));
        confirmationMessage.setLog(createFailedLog(MessageSeeds.BULK_ITEM_PROCESSING_WAS_NOT_STARTED.getDefaultFormat(null)));
        return confirmationMessage;
    }

    private UtilsDvceERPSmrtMtrCrteConfUtilsDvce createChildBody(String sapDeviceId) {
        UtilitiesDeviceID deviceId = objectFactory.createUtilitiesDeviceID();
        deviceId.setValue(sapDeviceId);

        UtilsDvceERPSmrtMtrCrteConfUtilsDvce device = objectFactory.createUtilsDvceERPSmrtMtrCrteConfUtilsDvce();
        device.setID(deviceId);

        return device;
    }

    private BusinessDocumentMessageHeader createChildHeader(Instant now) {
        BusinessDocumentMessageHeader header = objectFactory.createBusinessDocumentMessageHeader();

        header.setCreationDateTime(now);
        return header;
    }

    private BusinessDocumentMessageHeader createMessageHeader(String requestId, String referenceUuid, Instant now) {
        String uuid = UUID.randomUUID().toString();

        BusinessDocumentMessageHeader header = objectFactory.createBusinessDocumentMessageHeader();
        if (!Strings.isNullOrEmpty(requestId)){
            header.setReferenceID(createID(requestId));
        }
        header.setUUID(createUUID(uuid));
        if (!Strings.isNullOrEmpty(referenceUuid)){
            header.setReferenceUUID(createUUID(referenceUuid));
        }
        header.setCreationDateTime(now);
        return header;
    }

    private BusinessDocumentMessageID createID(String id) {
        BusinessDocumentMessageID messageID = objectFactory.createBusinessDocumentMessageID();
        messageID.setValue(id);
        return messageID;
    }

    private com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreateconfirmation.UUID createUUID(String uuid) {
        com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreateconfirmation.UUID messageUUID
                = objectFactory.createUUID();
        messageUUID.setValue(uuid);
        return messageUUID;
    }

    private Log createSuccessfulLog() {
        Log log = objectFactory.createLog();
        log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.SUCCESSFUL.getCode());
        log.getItem().add(createLogItem(MessageSeeds.OK_RESULT.getDefaultFormat(null),
                SUCCESSFUL_PROCESSING_TYPE_ID, SeverityCode.INFORMATION.getCode(),
                null));
        setMaximumLogItemSeverityCode(log);

        return log;
    }

    private Log createPartiallySuccessfulLog() {
        Log log = objectFactory.createLog();
        log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.PARTIALLY_SUCCESSFUL.getCode());
        log.getItem().add(createLogItem(MessageSeeds.PARTIALLY_SUCCESSFUL.getDefaultFormat(null),
                UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID, SeverityCode.ERROR.getCode(),
                null));
        setMaximumLogItemSeverityCode(log);

        return log;
    }

    private Log createFailedLog(String message) {
        Log log = objectFactory.createLog();
        log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.FAILED.getCode());
        log.getItem().add(createLogItem(message, UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID,
                SeverityCode.ERROR.getCode(), PROCESSING_ERROR_CATEGORY_CODE));
        setMaximumLogItemSeverityCode(log);

        return log;
    }

    private LogItem createLogItem(String message, String typeId, String severityCode, String categoryCode) {
        LogItem logItem = objectFactory.createLogItem();
        if (!Strings.isNullOrEmpty(categoryCode)) {
            LogItemCategoryCode logItemCategoryCode = objectFactory.createLogItemCategoryCode();
            logItemCategoryCode.setValue(categoryCode);
            logItem.setCategoryCode(logItemCategoryCode);
        }
        logItem.setSeverityCode(severityCode);
        logItem.setTypeID(typeId);
        logItem.setNote(message);

        return logItem;
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
