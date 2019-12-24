/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation;

import com.elster.jupiter.servicecall.DefaultState;
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
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreateconfirmation.Log;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreateconfirmation.LogItem;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreateconfirmation.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreateconfirmation.LogItemCategoryCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreateconfirmation.ObjectFactory;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreateconfirmation.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreateconfirmation.UtilsDvceERPSmrtMtrRegCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreateconfirmation.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdeviceregistercreateconfirmation.UtilsDvceERPSmrtMtrRegCrteConfUtilsDvce;

import com.google.common.base.Strings;

import java.time.Instant;
import java.util.List;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.elster.jupiter.servicecall.DefaultState.CANCELLED;
import static com.elster.jupiter.servicecall.DefaultState.FAILED;
import static com.elster.jupiter.servicecall.DefaultState.SUCCESSFUL;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.PROCESSING_ERROR_CATEGORY_CODE;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.SUCCESSFUL_PROCESSING_TYPE_ID;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID;

public class CreateRegisterConfirmationMessageFactory {

    private static final ObjectFactory objectFactory = new ObjectFactory();

    public CreateRegisterConfirmationMessageFactory() {
    }

    public UtilsDvceERPSmrtMtrRegCrteConfMsg createMessage(ServiceCall parent, ServiceCall deviceServiceCall, String senderBusinessSystemId, Instant now) {
        MasterUtilitiesDeviceRegisterCreateRequestDomainExtension extension = parent.getExtensionFor(new MasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet()).get();

        UtilsDvceERPSmrtMtrRegCrteConfMsg confirmationMessage = objectFactory.createUtilsDvceERPSmrtMtrRegCrteConfMsg();
        confirmationMessage.setMessageHeader(createHeader(extension.getRequestID(), extension.getUuid(), senderBusinessSystemId, now));

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
                            // unreachable case
                            confirmationMessage.setLog(createFailedLog(MessageSeeds.UNKNOWN_ERROR.getDefaultFormat(null)));
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

    public UtilsDvceERPSmrtMtrRegCrteConfMsg createMessage(UtilitiesDeviceRegisterCreateRequestMessage requestMessage, MessageSeeds messageSeed, String senderBusinessSystemId, Instant now) {
        UtilsDvceERPSmrtMtrRegCrteConfMsg confirmationMessage = objectFactory.createUtilsDvceERPSmrtMtrRegCrteConfMsg();
        confirmationMessage.setMessageHeader(createHeader(requestMessage.getRequestID(), requestMessage.getUuid(), senderBusinessSystemId, now));
        if(!requestMessage.getUtilitiesDeviceRegisterCreateMessages().isEmpty()) {
            confirmationMessage.setUtilitiesDevice(createUtilsDvce(requestMessage.getUtilitiesDeviceRegisterCreateMessages().get(0).getDeviceId()));
        }
        confirmationMessage.setLog(createFailedLog(messageSeed.getDefaultFormat(null)));
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
                    return "["+ extension.getLrn() + "] - " + extension.getErrorMessage();
                }).collect(Collectors.joining("; "));
    }

    private BusinessDocumentMessageHeader createHeader(String requestId, String referenceUuid, String senderBusinessSystemId, Instant now) {
        BusinessDocumentMessageHeader header = objectFactory.createBusinessDocumentMessageHeader();

        if (!Strings.isNullOrEmpty(requestId)){
            header.setReferenceID(createID(requestId));
        }
        if (!Strings.isNullOrEmpty(referenceUuid)){
            header.setReferenceUUID(createUUID(referenceUuid));
        }

        String uuid = UUID.randomUUID().toString();
        header.setUUID(createUUID(uuid));
        header.setSenderBusinessSystemID(senderBusinessSystemId);
        header.setReconciliationIndicator(true);
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
        log.getItem().add(createLogItem(MessageSeeds.OK_RESULT.getDefaultFormat(null),
                SUCCESSFUL_PROCESSING_TYPE_ID, SeverityCode.INFORMATION.getCode(),
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