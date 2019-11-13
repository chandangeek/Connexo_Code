/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.ProcessingResultCode;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.UtilitiesDeviceCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.UtilitiesDeviceCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.Log;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.LogItem;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.LogItemCategoryCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.ObjectFactory;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.UtilsDvceERPSmrtMtrCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.UtilsDvceERPSmrtMtrCrteConfUtilsDvce;

import java.time.Instant;
import java.util.UUID;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID;

public class CreateConfirmationMessageFactory {
    private static final ObjectFactory objectFactory = new ObjectFactory();

    public CreateConfirmationMessageFactory() {
    }

    public UtilsDvceERPSmrtMtrCrteConfMsg createMessage(ServiceCall parent, ServiceCall serviceCall, Instant now) {
        MasterUtilitiesDeviceCreateRequestDomainExtension extension = parent.getExtensionFor(new MasterUtilitiesDeviceCreateRequestCustomPropertySet()).get();

        UtilsDvceERPSmrtMtrCrteConfMsg confirmationMessage = objectFactory.createUtilsDvceERPSmrtMtrCrteConfMsg();
        confirmationMessage.setMessageHeader(createMessageHeader(extension.getRequestID(), extension.getUuid(), now));

        if (serviceCall.getState().equals(DefaultState.CANCELLED)) {
            confirmationMessage.setLog(createFailedLog(MessageSeeds.SERVICE_CALL_WAS_CANCELLED.getDefaultFormat(null)));
        } else if (serviceCall.getState().equals(DefaultState.SUCCESSFUL)) {
            confirmationMessage.setLog(createSuccessfulLog());
        } else if (serviceCall.getState().equals(DefaultState.FAILED)) {
            UtilitiesDeviceCreateRequestDomainExtension extensionChild = serviceCall.getExtensionFor(new UtilitiesDeviceCreateRequestCustomPropertySet()).get();
            confirmationMessage.setLog(createFailedLog(extensionChild.getErrorMessage()));
        }
        createBody(confirmationMessage, serviceCall, now);


        return confirmationMessage;
    }

    public UtilsDvceERPSmrtMtrCrteConfMsg createMessage(UtilitiesDeviceCreateRequestMessage message, MessageSeeds messageSeed, Instant now) {
        UtilsDvceERPSmrtMtrCrteConfMsg confirmMsg = objectFactory.createUtilsDvceERPSmrtMtrCrteConfMsg();
        confirmMsg.setMessageHeader(createMessageHeader(message.getRequestID(), message.getUuid(), now));
        if (!message.getUtilitiesDeviceCreateMessages().isEmpty()) {
            confirmMsg.setUtilitiesDevice(createUtilitiesDevice(message.getUtilitiesDeviceCreateMessages().get(0).getDeviceId()));
        }
        confirmMsg.setLog(createFailedLog(messageSeed.getDefaultFormat(null)));
        return confirmMsg;
    }

    private void createBody(UtilsDvceERPSmrtMtrCrteConfMsg confirmationMessage,
                            ServiceCall serviceCall, Instant now) {
        UtilitiesDeviceCreateRequestDomainExtension extension = serviceCall.getExtensionFor(new UtilitiesDeviceCreateRequestCustomPropertySet()).get();
        confirmationMessage.setUtilitiesDevice(createUtilitiesDevice(extension.getDeviceId()));
    }

    private BusinessDocumentMessageHeader createMessageHeader(String requestId, String referenceUuid, Instant now) {
        String uuid = UUID.randomUUID().toString();

        BusinessDocumentMessageHeader header = objectFactory.createBusinessDocumentMessageHeader();
        header.setReferenceID(createID(requestId));
        header.setUUID(createUUID(uuid));
        header.setReferenceUUID(createUUID(referenceUuid));
        header.setCreationDateTime(now);
        return header;
    }

    private UtilsDvceERPSmrtMtrCrteConfUtilsDvce createUtilitiesDevice(String strId) {
        UtilsDvceERPSmrtMtrCrteConfUtilsDvce device = objectFactory.createUtilsDvceERPSmrtMtrCrteConfUtilsDvce();
        UtilitiesDeviceID utDevice = objectFactory.createUtilitiesDeviceID();
        utDevice.setValue(strId);
        device.setID(utDevice);
        return device;
    }

    private Log createSuccessfulLog() {
        Log log = objectFactory.createLog();
        log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.SUCCESSFUL.getCode());
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
        logItemCategoryCode.setValue(WebServiceActivator.PROCESSING_ERROR_CATEGORY_CODE);

        LogItem logItem = objectFactory.createLogItem();
        logItem.setTypeID(UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID);
        logItem.setCategoryCode(logItemCategoryCode);
        logItem.setNote(message);

        return logItem;
    }

    private BusinessDocumentMessageID createID(String id) {
        BusinessDocumentMessageID messageID = objectFactory.createBusinessDocumentMessageID();
        messageID.setValue(id);
        return messageID;
    }

    private com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.UUID createUUID(String uuid) {
        com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicecreateconfirmation.UUID messageUUID
                = objectFactory.createUUID();
        messageUUID.setValue(uuid);
        return messageUUID;
    }
}
