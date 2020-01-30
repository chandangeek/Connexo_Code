/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation;

import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.util.streams.Predicates;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.ProcessingResultCode;
import com.energyict.mdc.sap.soap.webservices.impl.SeverityCode;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceRegisterCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.SubMasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.UtilitiesDeviceRegisterCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.UtilitiesDeviceRegisterCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreateconfirmation.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreateconfirmation.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreateconfirmation.Log;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreateconfirmation.LogItem;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreateconfirmation.LogItemCategoryCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreateconfirmation.ObjectFactory;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreateconfirmation.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreateconfirmation.UtilsDvceERPSmrtMtrRegBulkCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreateconfirmation.UtilsDvceERPSmrtMtrRegCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreateconfirmation.UtilsDvceERPSmrtMtrRegCrteConfUtilsDvce;

import com.google.common.base.Strings;

import java.time.Instant;
import java.util.List;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.elster.jupiter.servicecall.DefaultState.CANCELLED;
import static com.elster.jupiter.servicecall.DefaultState.FAILED;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.PROCESSING_ERROR_CATEGORY_CODE;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.SUCCESSFUL_PROCESSING_TYPE_ID;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID;

public class CreateBulkRegisterConfirmationMessageFactory {

    private static final ObjectFactory objectFactory = new ObjectFactory();

    public CreateBulkRegisterConfirmationMessageFactory() {
    }

    public UtilsDvceERPSmrtMtrRegBulkCrteConfMsg createMessage(ServiceCall parent, List<ServiceCall> children, String senderBusinessSystemId, Instant now) {
        MasterUtilitiesDeviceRegisterCreateRequestDomainExtension extension = parent.getExtensionFor(new MasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet()).get();

        UtilsDvceERPSmrtMtrRegBulkCrteConfMsg bulkConfirmationMessage = objectFactory.createUtilsDvceERPSmrtMtrRegBulkCrteConfMsg();

        bulkConfirmationMessage.setMessageHeader(createHeader(extension.getRequestID(), extension.getUuid(), senderBusinessSystemId, now));
        switch (parent.getState()) {
            case CANCELLED:
                bulkConfirmationMessage.setLog(createFailedLog(MessageSeeds.SERVICE_CALL_WAS_CANCELLED.getDefaultFormat(null)));
                break;
            case SUCCESSFUL:
                bulkConfirmationMessage.setLog(createSuccessfulLog());
                break;
            case FAILED:
                bulkConfirmationMessage.setLog(createFailedLog(MessageSeeds.BULK_REQUEST_WAS_FAILED.getDefaultFormat(null)));
                break;
            case PARTIAL_SUCCESS:
                bulkConfirmationMessage.setLog(createPartiallySuccessfulLog());
                break;
            default:
                // No specific action required for these states
                break;
        }

        createBody(bulkConfirmationMessage, children, senderBusinessSystemId, now);

        return bulkConfirmationMessage;
    }

    public UtilsDvceERPSmrtMtrRegBulkCrteConfMsg createMessage(UtilitiesDeviceRegisterCreateRequestMessage requestMessage, MessageSeeds messageSeed, String senderBusinessSystemId, Instant now, Object ...messageSeedArgs) {
        UtilsDvceERPSmrtMtrRegBulkCrteConfMsg bulkConfirmationMessage = objectFactory.createUtilsDvceERPSmrtMtrRegBulkCrteConfMsg();
        bulkConfirmationMessage.setMessageHeader(createHeader(requestMessage.getRequestID(), requestMessage.getUuid(), senderBusinessSystemId, now));
        requestMessage.getUtilitiesDeviceRegisterCreateMessages()
                .forEach(item -> {
                    bulkConfirmationMessage
                            .getUtilitiesDeviceERPSmartMeterRegisterCreateConfirmationMessage()
                            .add(createFailedChildMessage(item, senderBusinessSystemId, now));

                });
        bulkConfirmationMessage.setLog(createFailedLog(messageSeed.getDefaultFormat(messageSeedArgs)));
        return bulkConfirmationMessage;
    }

    private BusinessDocumentMessageHeader createHeader(String requestId, String referenceUuid, String senderBusinessSystemId, Instant now) {
        BusinessDocumentMessageHeader header = objectFactory.createBusinessDocumentMessageHeader();
        String uuid = UUID.randomUUID().toString();

        if (!Strings.isNullOrEmpty(requestId)){
            header.setReferenceID(createID(requestId));
        }
        header.setUUID(createUUID(uuid));
        if (!Strings.isNullOrEmpty(referenceUuid)){
            header.setReferenceUUID(createUUID(referenceUuid));
        }
        header.setSenderBusinessSystemID(senderBusinessSystemId);
        header.setReconciliationIndicator(true);
        header.setCreationDateTime(now);

        return header;
    }

    private BusinessDocumentMessageID createID(String id) {
        BusinessDocumentMessageID messageID = objectFactory.createBusinessDocumentMessageID();
        messageID.setValue(id);
        return messageID;
    }

    private com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreateconfirmation.UUID createUUID(String uuid) {
        com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreateconfirmation.UUID messageUUID
                = objectFactory.createUUID();
        messageUUID.setValue(uuid);
        return messageUUID;
    }

    private void createBody(UtilsDvceERPSmrtMtrRegBulkCrteConfMsg bulkConfirmationMessage,
                            List<ServiceCall> children, String senderBusinessSystemId, Instant now) {
        children.forEach(child -> {
            bulkConfirmationMessage.getUtilitiesDeviceERPSmartMeterRegisterCreateConfirmationMessage()
                    .add(createChildMessage(child, senderBusinessSystemId, now));
        });
    }

    private UtilsDvceERPSmrtMtrRegCrteConfMsg createChildMessage(ServiceCall childServiceCall, String senderBusinessSystemId, Instant now) {
        SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension extension = childServiceCall.getExtensionFor(new SubMasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet()).get();

        UtilsDvceERPSmrtMtrRegCrteConfMsg confirmationMessage = objectFactory.createUtilsDvceERPSmrtMtrRegCrteConfMsg();
        confirmationMessage.setMessageHeader(createChildHeader(extension.getRequestId(), extension.getUuid(), senderBusinessSystemId, now));
        confirmationMessage.setUtilitiesDevice(createChildBody(extension.getDeviceId()));

        List<ServiceCall> children = findChildren(childServiceCall);
        switch (childServiceCall.getState()) {
            case SUCCESSFUL:
                confirmationMessage.setLog(createSuccessfulLog());
                break;
            case FAILED:
                if (isDeviceNotFoundError(children)) {
                    confirmationMessage.setLog(createFailedLog(MessageSeeds.NO_DEVICE_FOUND_BY_SAP_ID.getDefaultFormat(extension.getDeviceId())));
                } else {
                    String failedRegisterError = createRegisterError(children);
                    if (!failedRegisterError.isEmpty()) {
                        confirmationMessage.setLog(createFailedLog(MessageSeeds.FAILED_DATA_SOURCE.getDefaultFormat(failedRegisterError)));
                    } else {
                        // unreachable case
                        confirmationMessage.setLog(createFailedLog(MessageSeeds.UNKNOWN_ERROR.getDefaultFormat(null)));
                    }
                }
                break;
            case CANCELLED:
                confirmationMessage.setLog(createFailedLog(MessageSeeds.SERVICE_CALL_WAS_CANCELLED.getDefaultFormat(null)));
                break;
            default:
                // No specific action required for these states
                break;
        }
        return confirmationMessage;
    }

    private UtilsDvceERPSmrtMtrRegCrteConfMsg createFailedChildMessage(UtilitiesDeviceRegisterCreateMessage message, String senderBusinessSystemId, Instant now) {
             UtilsDvceERPSmrtMtrRegCrteConfMsg confirmationMessage = objectFactory.createUtilsDvceERPSmrtMtrRegCrteConfMsg();
        confirmationMessage.setMessageHeader(createChildHeader(message.getRequestId(), message.getUuid(), senderBusinessSystemId, now));
        confirmationMessage.setUtilitiesDevice(createChildBody(message.getDeviceId()));
        confirmationMessage.setLog(createFailedLog(MessageSeeds.BULK_ITEM_PROCESSING_WAS_NOT_STARTED.getDefaultFormat(null)));
        return confirmationMessage;
    }

    private boolean isDeviceNotFoundError(List<ServiceCall> serviceCalls) {
        return serviceCalls.stream()
                .filter(child -> child.getState() == FAILED)
                .map(child -> child.getExtensionFor(new UtilitiesDeviceRegisterCreateRequestCustomPropertySet()).get())
                .anyMatch(each -> each.getErrorCode().equals(MessageSeeds.NO_DEVICE_FOUND_BY_SAP_ID.code()));
    }

    private String createRegisterError(List<ServiceCall> serviceCalls) {
        return serviceCalls.stream()
                .filter(child -> child.getState() == FAILED || child.getState() == CANCELLED)
                .map(child -> {
                    UtilitiesDeviceRegisterCreateRequestDomainExtension extension = child.getExtensionFor(new UtilitiesDeviceRegisterCreateRequestCustomPropertySet()).get();
                    return "["+ extension.getLrn() + "] - " + extension.getErrorMessage();
                }).collect(Collectors.joining("; "));
    }

    private UtilsDvceERPSmrtMtrRegCrteConfUtilsDvce createChildBody(String sapDeviceId) {
        UtilitiesDeviceID deviceId = objectFactory.createUtilitiesDeviceID();
        deviceId.setValue(sapDeviceId);

        UtilsDvceERPSmrtMtrRegCrteConfUtilsDvce device = objectFactory.createUtilsDvceERPSmrtMtrRegCrteConfUtilsDvce();
        device.setID(deviceId);

        return device;
    }

    private BusinessDocumentMessageHeader createChildHeader(String requestId, String uuid, String senderBusinessSystemId, Instant now) {
        BusinessDocumentMessageHeader header = objectFactory.createBusinessDocumentMessageHeader();

        if (!Strings.isNullOrEmpty(requestId)){
            header.setReferenceID(createID(requestId));
        }
        if (!Strings.isNullOrEmpty(uuid)){
            header.setReferenceUUID(createUUID(uuid));
        }
        header.setUUID(createUUID(java.util.UUID.randomUUID().toString()));
        header.setSenderBusinessSystemID(senderBusinessSystemId);
        header.setReconciliationIndicator(true);
        header.setCreationDateTime(now);
        return header;
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

    private List<ServiceCall> findChildren(ServiceCall serviceCall) {
        return serviceCall.findChildren().stream().collect(Collectors.toList());
    }
}