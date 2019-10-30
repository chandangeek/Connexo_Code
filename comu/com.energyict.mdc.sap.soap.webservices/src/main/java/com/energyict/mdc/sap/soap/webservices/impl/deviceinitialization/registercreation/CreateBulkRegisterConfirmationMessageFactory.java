/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.ProcessingResultCode;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceRegisterCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.SubMasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.UtilitiesDeviceRegisterCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.UtilitiesDeviceRegisterCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreateconfirmation.LogItem;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreateconfirmation.Log;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreateconfirmation.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreateconfirmation.LogItemCategoryCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreateconfirmation.ObjectFactory;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreateconfirmation.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreateconfirmation.UtilsDvceERPSmrtMtrRegBulkCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreateconfirmation.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreateconfirmation.UtilsDvceERPSmrtMtrRegCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregisterbulkcreateconfirmation.UtilsDvceERPSmrtMtrRegCrteConfUtilsDvce;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.elster.jupiter.servicecall.DefaultState.CANCELLED;
import static com.elster.jupiter.servicecall.DefaultState.FAILED;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.PROCESSING_ERROR_CATEGORY_CODE;

public class CreateBulkRegisterConfirmationMessageFactory {

    private static final ObjectFactory objectFactory = new ObjectFactory();

    public CreateBulkRegisterConfirmationMessageFactory() {
    }

    public UtilsDvceERPSmrtMtrRegBulkCrteConfMsg createMessage(ServiceCall parent, List<ServiceCall> children, Instant now) {
        MasterUtilitiesDeviceRegisterCreateRequestDomainExtension extension = parent.getExtensionFor(new MasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet()).get();

        UtilsDvceERPSmrtMtrRegBulkCrteConfMsg bulkConfirmationMessage = objectFactory.createUtilsDvceERPSmrtMtrRegBulkCrteConfMsg();

        bulkConfirmationMessage.setMessageHeader(createHeader(extension.getRequestID(), extension.getUuid(), now));
        switch (parent.getState()) {
            case CANCELLED:
                bulkConfirmationMessage.setLog(createFailedLog(String.valueOf(MessageSeeds.SERVICE_CALL_WAS_CANCELLED.getNumber()),
                        MessageSeeds.SERVICE_CALL_WAS_CANCELLED.getDefaultFormat(null)));
                break;
            case SUCCESSFUL:
                bulkConfirmationMessage.setLog(createSuccessfulLog());
                break;
            case FAILED:
                bulkConfirmationMessage.setLog(createFailedLog());
                break;
            case PARTIAL_SUCCESS:
                bulkConfirmationMessage.setLog(createPartiallySuccessfulLog());
                break;
            default:
                // No specific action required for these states
                break;
        }

        createBody(bulkConfirmationMessage, children, now);

        return bulkConfirmationMessage;
    }

    public UtilsDvceERPSmrtMtrRegBulkCrteConfMsg createMessage(UtilitiesDeviceRegisterCreateRequestMessage requestMessage, MessageSeeds messageSeed, Instant now) {
        UtilsDvceERPSmrtMtrRegBulkCrteConfMsg bulkConfirmationMessage = objectFactory.createUtilsDvceERPSmrtMtrRegBulkCrteConfMsg();
        bulkConfirmationMessage.setMessageHeader(createHeader(requestMessage.getRequestID(), requestMessage.getUuid(), now));
        bulkConfirmationMessage.setLog(objectFactory.createLog());
        bulkConfirmationMessage.getLog().getItem().add(createLogItem(messageSeed));
        return bulkConfirmationMessage;
    }

    private BusinessDocumentMessageHeader createHeader(String requestId, String referenceUuid, Instant now) {
        BusinessDocumentMessageHeader header = objectFactory.createBusinessDocumentMessageHeader();
        String uuid = UUID.randomUUID().toString();

        header.setReferenceID(createID(requestId));
        header.setUUID(createUUID(uuid));
        header.setReferenceUUID(createUUID(referenceUuid));
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
                            List<ServiceCall> children, Instant now) {
        children.forEach(child -> {
            bulkConfirmationMessage.getUtilitiesDeviceERPSmartMeterRegisterCreateConfirmationMessage()
                    .add(createChildMessage(child, now));
        });
    }

    private UtilsDvceERPSmrtMtrRegCrteConfMsg createChildMessage(ServiceCall childServiceCall, Instant now) {
        SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension extension = childServiceCall.getExtensionFor(new SubMasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet()).get();

        UtilsDvceERPSmrtMtrRegCrteConfMsg confirmationMessage = objectFactory.createUtilsDvceERPSmrtMtrRegCrteConfMsg();
        confirmationMessage.setMessageHeader(createChildHeader(now));
        confirmationMessage.setUtilitiesDevice(createChildBody(extension.getDeviceId()));

        List<ServiceCall> children = findChildren(childServiceCall);
        switch (childServiceCall.getState()) {
            case SUCCESSFUL:
                confirmationMessage.setLog(createSuccessfulLog());
                break;
            case FAILED:
                if (isDeviceNotFoundError(children)) {
                    confirmationMessage.setLog(createFailedLog(MessageSeeds.NO_DEVICE_FOUND_BY_SAP_ID.code(), MessageSeeds.NO_DEVICE_FOUND_BY_SAP_ID.getDefaultFormat(extension.getDeviceId())));
                } else {
                    String failedRegisterError = createRegisterError(children);
                    if (!failedRegisterError.isEmpty()) {
                        confirmationMessage.setLog(createFailedLog(MessageSeeds.FAILED_DATA_SOURCE.code(), MessageSeeds.FAILED_DATA_SOURCE.getDefaultFormat(failedRegisterError)));
                    } else {
                        confirmationMessage.setLog(createFailedLog());
                    }
                }
                break;
            case CANCELLED:
                confirmationMessage.setLog(createFailedLog(MessageSeeds.SERVICE_CALL_WAS_CANCELLED.code(), MessageSeeds.SERVICE_CALL_WAS_CANCELLED.getDefaultFormat(null)));
                break;
            default:
                // No specific action required for these states
                break;
        }
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
                    return extension.getErrorMessage();
                }).collect(Collectors.joining("; "));
    }

    private UtilsDvceERPSmrtMtrRegCrteConfUtilsDvce createChildBody(String sapDeviceId) {
        UtilitiesDeviceID deviceId = objectFactory.createUtilitiesDeviceID();
        deviceId.setValue(sapDeviceId);

        UtilsDvceERPSmrtMtrRegCrteConfUtilsDvce device = objectFactory.createUtilsDvceERPSmrtMtrRegCrteConfUtilsDvce();
        device.setID(deviceId);

        return device;
    }

    private BusinessDocumentMessageHeader createChildHeader(Instant now) {
        BusinessDocumentMessageHeader header = objectFactory.createBusinessDocumentMessageHeader();

        header.setCreationDateTime(now);
        return header;
    }

    private LogItem createLogItem(MessageSeeds messageSeeds, Object... args) {
        LogItemCategoryCode logItemCategoryCode = objectFactory.createLogItemCategoryCode();
        logItemCategoryCode.setValue(PROCESSING_ERROR_CATEGORY_CODE);

        LogItem logItem = objectFactory.createLogItem();
        logItem.setTypeID(String.valueOf(messageSeeds.getNumber()));
        logItem.setCategoryCode(logItemCategoryCode);
        logItem.setNote(messageSeeds.getDefaultFormat(args));

        return logItem;
    }

    private Log createSuccessfulLog() {
        Log log = objectFactory.createLog();
        log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.SUCCESSFUL.getCode());
        return log;
    }

    private Log createFailedLog() {
        Log log = objectFactory.createLog();
        log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.FAILED.getCode());
        return log;
    }

    private Log createPartiallySuccessfulLog() {
        Log log = objectFactory.createLog();
        log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.PARTIALLY_SUCCESSFUL.getCode());
        return log;
    }

    private Log createFailedLog(String code, String message) {
        Log log = objectFactory.createLog();
        log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.FAILED.getCode());
        log.getItem().add(createLogItem(code, message));
        return log;
    }

    private LogItem createLogItem(String code, String message) {
        LogItemCategoryCode logItemCategoryCode = objectFactory.createLogItemCategoryCode();
        logItemCategoryCode.setValue(PROCESSING_ERROR_CATEGORY_CODE);

        LogItem logItem = objectFactory.createLogItem();
        logItem.setTypeID(code);
        logItem.setCategoryCode(logItemCategoryCode);
        logItem.setNote(message);

        return logItem;
    }

    private List<ServiceCall> findChildren(ServiceCall serviceCall) {
        return serviceCall.findChildren().stream().collect(Collectors.toList());
    }
}