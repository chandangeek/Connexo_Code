/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.registercreation;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;

import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceRegisterCreateRequestDomainExtension;

import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.SubMasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.UtilitiesDeviceRegisterCreateRequestCustomPropertySet;
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

public class CreateRegisterConfirmationMessageFactory {

    private static final ObjectFactory objectFactory = new ObjectFactory();

    public CreateRegisterConfirmationMessageFactory() {
    }

    public UtilsDvceERPSmrtMtrRegCrteConfMsg createMessage(ServiceCall parent, List<ServiceCall> children, Instant now) {
        MasterUtilitiesDeviceRegisterCreateRequestDomainExtension extension = parent.getExtensionFor(new MasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet()).get();

        UtilsDvceERPSmrtMtrRegCrteConfMsg bulkConfirmationMessage = objectFactory.createUtilsDvceERPSmrtMtrRegCrteConfMsg();

        bulkConfirmationMessage.setMessageHeader(createHeader(extension.getRequestID(), now));
        if (parent.getState().equals(DefaultState.CANCELLED)) {
            bulkConfirmationMessage.setLog(createFailedLog(String.valueOf(MessageSeeds.SERVICE_CALL_WAS_CANCELLED.getNumber()), MessageSeeds.SERVICE_CALL_WAS_CANCELLED.getDefaultFormat()));
        } else if (parent.getState().equals(DefaultState.SUCCESSFUL)) {
            bulkConfirmationMessage.setLog(createSuccessfulLog());
        } else if (parent.getState().equals(DefaultState.FAILED)) {
            bulkConfirmationMessage.setLog(createFailedLog());
        }

        createBody(bulkConfirmationMessage, children, now);

        return bulkConfirmationMessage;
    }

    public UtilsDvceERPSmrtMtrRegCrteConfMsg createMessage(UtilitiesDeviceRegisterCreateRequestMessage requestMessage, MessageSeeds messageSeed, Instant now) {
        UtilsDvceERPSmrtMtrRegCrteConfMsg bulkConfirmationMessage =  objectFactory.createUtilsDvceERPSmrtMtrRegCrteConfMsg();
        bulkConfirmationMessage.setMessageHeader(createHeader(requestMessage.getRequestID(), now));
        bulkConfirmationMessage.setLog(objectFactory.createLog());
        bulkConfirmationMessage.getLog().getItem().add(createLogItem(messageSeed));
        return bulkConfirmationMessage;
    }

    private void createBody(UtilsDvceERPSmrtMtrRegCrteConfMsg bulkConfirmationMessage,
                            List<ServiceCall> children, Instant now) {
                createChildMessage(children.get(0), now);
    }

    private UtilsDvceERPSmrtMtrRegCrteConfMsg createChildMessage(ServiceCall childServiceCall, Instant now) {
        SubMasterUtilitiesDeviceRegisterCreateRequestDomainExtension extension = childServiceCall.getExtensionFor(new SubMasterUtilitiesDeviceRegisterCreateRequestCustomPropertySet()).get();

        UtilsDvceERPSmrtMtrRegCrteConfMsg confirmationMessage = objectFactory.createUtilsDvceERPSmrtMtrRegCrteConfMsg();
        confirmationMessage.setMessageHeader(createChildHeader(now));
        confirmationMessage.setUtilitiesDevice(createChildBody(extension.getDeviceId()));
        if (childServiceCall.getState() == DefaultState.SUCCESSFUL) {
            List<ServiceCall> children = findChildren(childServiceCall);
            if (hasAllChildState(children, DefaultState.SUCCESSFUL)) {
                confirmationMessage.setLog(createSuccessfulLog());
            } else {
                List<String> failedRegister = children.stream()
                        .map(child->child.getExtensionFor(new UtilitiesDeviceRegisterCreateRequestCustomPropertySet()).get())
                        .map(ext->ext.getRegisterId())
                        .collect(Collectors.toList());
                confirmationMessage.setLog(createFailedLog(MessageSeeds.FAILED_REGISTER.code(), MessageSeeds.FAILED_REGISTER.getDefaultFormat(failedRegister.toString())));
            }
        } else if (childServiceCall.getState() == DefaultState.FAILED) {
            confirmationMessage.setLog(createFailedLog());
        }
        return confirmationMessage;
    }

    private BusinessDocumentMessageHeader createHeader(String requestId, Instant now) {
        BusinessDocumentMessageHeader header = objectFactory.createBusinessDocumentMessageHeader();
        String uuid = UUID.randomUUID().toString();

        header.setReferenceID(createID(requestId));
        header.setUUID(createUUID(uuid));
        header.setCreationDateTime(now);

        return header;
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
        log.setBusinessDocumentProcessingResultCode("3");
        return log;
    }

    private Log createFailedLog() {
        Log log = objectFactory.createLog();
        log.setBusinessDocumentProcessingResultCode("5");
        return log;
    }

    private Log createFailedLog(String code, String message) {
        Log log = objectFactory.createLog();
        log.setBusinessDocumentProcessingResultCode("5");
        log.getItem().add(createLogItem(code, message));
        return log;
    }

    private LogItem createLogItem(String code, String message) {
        LogItemCategoryCode logItemCategoryCode = objectFactory.createLogItemCategoryCode();
        logItemCategoryCode.setValue("PRE");

        LogItem logItem = objectFactory.createLogItem();
        logItem.setTypeID(code);
        logItem.setCategoryCode(logItemCategoryCode);
        logItem.setNote(message);

        return logItem;
    }

    private LogItem createLogItem(MessageSeeds messageSeeds, Object... args) {
        LogItemCategoryCode logItemCategoryCode = objectFactory.createLogItemCategoryCode();
        logItemCategoryCode.setValue("PRE");

        LogItem logItem = objectFactory.createLogItem();
        logItem.setTypeID(String.valueOf(messageSeeds.getNumber()));
        logItem.setCategoryCode(logItemCategoryCode);
        logItem.setNote(messageSeeds.getDefaultFormat(args));

        return logItem;
    }

    private List<ServiceCall> findChildren(ServiceCall serviceCall) {
        return serviceCall.findChildren().stream().collect(Collectors.toList());
    }

    private boolean hasAllChildState(List<ServiceCall> serviceCalls, DefaultState defaultState) {
        return serviceCalls.stream().allMatch(sc -> sc.getState().equals(defaultState));
    }
}