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
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreateconfirmation.Log;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreateconfirmation.LogItem;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreateconfirmation.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreateconfirmation.LogItemCategoryCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreateconfirmation.ObjectFactory;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreateconfirmation.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreateconfirmation.UtilsDvceERPSmrtMtrRegCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreateconfirmation.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreateconfirmation.UtilsDvceERPSmrtMtrRegCrteConfUtilsDvce;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.elster.jupiter.servicecall.DefaultState.CANCELLED;
import static com.elster.jupiter.servicecall.DefaultState.FAILED;
import static com.elster.jupiter.servicecall.DefaultState.SUCCESSFUL;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.PROCESSING_ERROR_CATEGORY_CODE;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID;

public class CreateRegisterConfirmationMessageFactory {

    private static final ObjectFactory objectFactory = new ObjectFactory();

    public CreateRegisterConfirmationMessageFactory() {
    }

    public UtilsDvceERPSmrtMtrRegCrteConfMsg createMessage(ServiceCall parent, ServiceCall deviceServiceCall, Instant now) {
        MasterUtilitiesDeviceRegisterCreateRequestDomainExtension extension = parent.getExtensionFor(new MasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet()).get();

        UtilsDvceERPSmrtMtrRegCrteConfMsg confirmationMessage = objectFactory.createUtilsDvceERPSmrtMtrRegCrteConfMsg();
        confirmationMessage.setMessageHeader(createHeader(extension.getRequestID(), extension.getUuid(), now));

        SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension subExtension = deviceServiceCall.getExtensionFor(new SubMasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet()).get();

        switch (parent.getState()) {
            case SUCCESSFUL:
                confirmationMessage.setLog(createSuccessfulLog());
                break;
            case FAILED:
                List<ServiceCall> registerServiceCalls = findChildren(deviceServiceCall);
                if (deviceServiceCall.getState() == SUCCESSFUL) {
                    confirmationMessage.setLog(createSuccessfulLog());
                } else if (deviceServiceCall.getState() == FAILED) {
                    if (isDeviceNotFoundError(registerServiceCalls)) {
                        confirmationMessage.setLog(createFailedLog(MessageSeeds.NO_DEVICE_FOUND_BY_SAP_ID.getDefaultFormat(subExtension.getDeviceId())));
                    } else {
                        String failedRegisterError = createRegisterError(registerServiceCalls);
                        if (!failedRegisterError.isEmpty()) {
                            confirmationMessage.setLog(createFailedLog(MessageSeeds.FAILED_DATA_SOURCE.getDefaultFormat(failedRegisterError)));
                        } else {
                            confirmationMessage.setLog(createFailedLog());
                        }
                    }
                }else if (deviceServiceCall.getState() == CANCELLED) {
                    confirmationMessage.setLog(createFailedLog(MessageSeeds.SERVICE_CALL_WAS_CANCELLED.getDefaultFormat(null)));
                }
                break;
            case CANCELLED:
                confirmationMessage.setLog(createFailedLog(MessageSeeds.SERVICE_CALL_WAS_CANCELLED.getDefaultFormat(null)));
                break;
            default:
                // No specific action required for these states
                break;
        }

        createBody(confirmationMessage, deviceServiceCall, now);

        return confirmationMessage;
    }

    public UtilsDvceERPSmrtMtrRegCrteConfMsg createMessage(UtilitiesDeviceRegisterCreateRequestMessage requestMessage, MessageSeeds messageSeed, Instant now) {
        UtilsDvceERPSmrtMtrRegCrteConfMsg confirmationMessage = objectFactory.createUtilsDvceERPSmrtMtrRegCrteConfMsg();
        confirmationMessage.setMessageHeader(createHeader(requestMessage.getRequestID(), requestMessage.getUuid(), now));
        confirmationMessage.setLog(objectFactory.createLog());
        confirmationMessage.getLog().getItem().add(createLogItem(messageSeed));
        return confirmationMessage;
    }

    private void createBody(UtilsDvceERPSmrtMtrRegCrteConfMsg confirmationMessage,
                            ServiceCall serviceCall, Instant now) {
        SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension extension = serviceCall.getExtensionFor(new SubMasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet()).get();
        confirmationMessage.setUtilitiesDevice(createUtilsDvce(extension.getDeviceId()));
    }

    private boolean isDeviceNotFoundError(List<ServiceCall> serviceCalls) {
        return serviceCalls.stream()
                .filter(child -> child.getState() == DefaultState.FAILED)
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

    private BusinessDocumentMessageHeader createHeader(String requestId, String referenceUuid, Instant now) {
        BusinessDocumentMessageHeader header = objectFactory.createBusinessDocumentMessageHeader();
        String uuid = UUID.randomUUID().toString();

        header.setReferenceID(createID(requestId));
        header.setUUID(createUUID(uuid));
        header.setReferenceUUID(createUUID(referenceUuid));
        header.setCreationDateTime(now);

        return header;
    }

    private UtilsDvceERPSmrtMtrRegCrteConfUtilsDvce createUtilsDvce(String sapDeviceId) {
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

    private BusinessDocumentMessageID createID(String id) {
        BusinessDocumentMessageID messageID = objectFactory.createBusinessDocumentMessageID();
        messageID.setValue(id);
        return messageID;
    }

    private com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreateconfirmation.UUID createUUID(String uuid) {
        com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreateconfirmation.UUID messageUUID
                = objectFactory.createUUID();
        messageUUID.setValue(uuid);
        return messageUUID;
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

    private Log createFailedLog(String message) {
        Log log = objectFactory.createLog();
        log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.FAILED.getCode());
        log.getItem().add(createLogItem(message));
        return log;
    }

    private LogItem createLogItem(String message) {
        LogItemCategoryCode logItemCategoryCode = objectFactory.createLogItemCategoryCode();
        logItemCategoryCode.setValue(PROCESSING_ERROR_CATEGORY_CODE);

        LogItem logItem = objectFactory.createLogItem();
        logItem.setTypeID(UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID);
        logItem.setCategoryCode(logItemCategoryCode);
        logItem.setNote(message);

        return logItem;
    }

    private LogItem createLogItem(MessageSeeds messageSeeds, Object... args) {
        LogItemCategoryCode logItemCategoryCode = objectFactory.createLogItemCategoryCode();
        logItemCategoryCode.setValue(PROCESSING_ERROR_CATEGORY_CODE);

        LogItem logItem = objectFactory.createLogItem();
        logItem.setTypeID(UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID);
        logItem.setCategoryCode(logItemCategoryCode);
        logItem.setNote(messageSeeds.getDefaultFormat(args));

        return logItem;
    }

    private List<ServiceCall> findChildren(ServiceCall serviceCall) {
        return serviceCall.findChildren().stream().collect(Collectors.toList());
    }
}