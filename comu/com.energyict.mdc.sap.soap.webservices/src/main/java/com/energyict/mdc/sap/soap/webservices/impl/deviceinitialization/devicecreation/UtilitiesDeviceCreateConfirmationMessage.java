/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.devicecreation;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.MasterUtilitiesDeviceCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.UtilitiesDeviceCreateRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization.UtilitiesDeviceCreateRequestDomainExtension;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreateconfirmation.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreateconfirmation.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreateconfirmation.Log;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreateconfirmation.LogItemCategoryCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreateconfirmation.ObjectFactory;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreateconfirmation.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreateconfirmation.UtilsDvceERPSmrtMtrBlkCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreateconfirmation.UtilsDvceERPSmrtMtrCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreateconfirmation.UtilsDvceERPSmrtMtrCrteConfUtilsDvce;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicebulkcreateconfirmation.LogItem;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class UtilitiesDeviceCreateConfirmationMessage {

    private final ObjectFactory objectFactory = new ObjectFactory();
    private UtilsDvceERPSmrtMtrBlkCrteConfMsg confirmationMessage;

    public UtilsDvceERPSmrtMtrBlkCrteConfMsg getConfirmationMessage() {
        return confirmationMessage;
    }

    public static Builder builder() {
        return new UtilitiesDeviceCreateConfirmationMessage().new Builder();
    }

    public class Builder {

        private Builder() {
        }

        public Builder from(ServiceCall parent, List<ServiceCall> children, Instant now) {
            MasterUtilitiesDeviceCreateRequestDomainExtension extension = parent.getExtensionFor(new MasterUtilitiesDeviceCreateRequestCustomPropertySet()).get();
            confirmationMessage = objectFactory.createUtilsDvceERPSmrtMtrBlkCrteConfMsg();
            confirmationMessage.setMessageHeader(createMessageHeader(extension.getRequestID(), now));

            if (parent.getState().equals(DefaultState.CANCELLED)) {
                confirmationMessage.setLog(createFailedLog(String.valueOf(MessageSeeds.SERVICE_CALL_WAS_CANCELLED.getNumber()), MessageSeeds.SERVICE_CALL_WAS_CANCELLED.getDefaultFormat()));
            } else if (hasAllChildState(children, DefaultState.SUCCESSFUL)) {
                confirmationMessage.setLog(createSuccessfulLog());
            } else {
                confirmationMessage.setLog(createFailedLog());
            }

            createBody(confirmationMessage, children, now);
            return this;
        }

        public Builder from(UtilitiesDeviceCreateRequestMessage message, MessageSeeds messageSeed, Instant now) {
            confirmationMessage = objectFactory.createUtilsDvceERPSmrtMtrBlkCrteConfMsg();
            confirmationMessage.setMessageHeader(createMessageHeader(message.getRequestID(), now));

            confirmationMessage.setLog(createFailedLog(String.valueOf(messageSeed.getNumber()), messageSeed.getDefaultFormat()));
            return this;
        }

        public UtilitiesDeviceCreateConfirmationMessage build() {
            return UtilitiesDeviceCreateConfirmationMessage.this;
        }

        private boolean hasAllChildState(List<ServiceCall> serviceCalls, DefaultState defaultState) {
            return serviceCalls.stream().allMatch(sc -> sc.getState().equals(defaultState));
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
                confirmationMessage.setLog(createFailedLog(extension.getErrorCode(), extension.getErrorMessage()));
            }
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

        private BusinessDocumentMessageHeader createMessageHeader(String requestId, Instant now) {
            String uuid = UUID.randomUUID().toString();

            BusinessDocumentMessageHeader header = objectFactory.createBusinessDocumentMessageHeader();
            header.setReferenceID(createID(requestId));
            header.setUUID(createUUID(uuid));
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
    }

}
