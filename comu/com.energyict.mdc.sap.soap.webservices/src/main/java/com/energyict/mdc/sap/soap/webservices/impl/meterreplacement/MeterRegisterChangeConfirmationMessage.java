/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.meterreplacement;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.ProcessingResultCode;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallHelper;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.MasterMeterRegisterChangeRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.MasterMeterRegisterChangeRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.MeterRegisterChangeRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.MeterRegisterChangeRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.SubMasterMeterRegisterChangeRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.SubMasterMeterRegisterChangeRequestDomainExtension;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementconfirmation.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementconfirmation.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementconfirmation.Log;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementconfirmation.LogItem;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementconfirmation.LogItemCategoryCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementconfirmation.ObjectFactory;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementconfirmation.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementconfirmation.UtilsDvceERPSmrtMtrRegChgConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementconfirmation.UtilsDvceERPSmrtMtrRegChgConfUtilsDvce;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID;

public class MeterRegisterChangeConfirmationMessage {
    private final ObjectFactory objectFactory = new ObjectFactory();
    private UtilsDvceERPSmrtMtrRegChgConfMsg confirmationMessage;

    public UtilsDvceERPSmrtMtrRegChgConfMsg getConfirmationMessage() {
        return confirmationMessage;
    }

    public static MeterRegisterChangeConfirmationMessage.Builder builder() {
        return new MeterRegisterChangeConfirmationMessage().new Builder();
    }

    public class Builder {

        private Builder() {
        }

        public MeterRegisterChangeConfirmationMessage.Builder from(ServiceCall parent, Instant now) {
            MasterMeterRegisterChangeRequestDomainExtension extension = parent.getExtensionFor(new MasterMeterRegisterChangeRequestCustomPropertySet()).get();
            ServiceCall child = parent.findChildren().stream().findFirst().orElseThrow(() -> new IllegalStateException("Unable to get child service call"));
            confirmationMessage = objectFactory.createUtilsDvceERPSmrtMtrRegChgConfMsg();
            confirmationMessage.setMessageHeader(createMessageHeader(extension.getRequestId(), extension.getUuid(), now));

            if (parent.getState().equals(DefaultState.CANCELLED)) {
                confirmationMessage.setLog(createFailedLog(MessageSeeds.SERVICE_CALL_WAS_CANCELLED.getDefaultFormat(null)));
            } else if (parent.getState().equals(DefaultState.SUCCESSFUL)) {
                confirmationMessage.setLog(createSuccessfulLog());
            } else if (parent.getState().equals(DefaultState.PARTIAL_SUCCESS)) {
                confirmationMessage.setLog(createPartiallySuccessfulLog());
            } else {
                confirmationMessage.setLog(createFailedLog());
            }

            createBody(confirmationMessage, child, now);
            return this;
        }

        public MeterRegisterChangeConfirmationMessage.Builder from(MeterRegisterChangeMessage message, MessageSeeds messageSeed, Instant now) {
            confirmationMessage = objectFactory.createUtilsDvceERPSmrtMtrRegChgConfMsg();
            confirmationMessage.setMessageHeader(createMessageHeader(message.getId(), message.getUuid(), now));
            confirmationMessage.setUtilitiesDevice(createChildBody(message.getDeviceId()));
            confirmationMessage.setLog(createFailedLog(messageSeed.getDefaultFormat(null)));
            return this;
        }

        public MeterRegisterChangeConfirmationMessage build() {
            return MeterRegisterChangeConfirmationMessage.this;
        }

        private void createBody(UtilsDvceERPSmrtMtrRegChgConfMsg confirmationMessage, ServiceCall subParent, Instant now) {
            SubMasterMeterRegisterChangeRequestDomainExtension extension = subParent.getExtensionFor(new SubMasterMeterRegisterChangeRequestCustomPropertySet())
                    .orElseThrow(() -> new IllegalStateException("Can not find domain extension for service call"));

            confirmationMessage.setMessageHeader(createMessageHeader(extension.getRequestId(), extension.getUuid(), now));
            confirmationMessage.setUtilitiesDevice(createChildBody(extension.getDeviceId()));
            if (subParent.getState() == DefaultState.SUCCESSFUL) {
                confirmationMessage.setLog(createSuccessfulLog());
            } else if (subParent.getState() == DefaultState.FAILED || subParent.getState() == DefaultState.PARTIAL_SUCCESS || subParent.getState() == DefaultState.CANCELLED) {
                Optional<String> errorMessage = ServiceCallHelper.findChildren(subParent).stream()
                        .map(child -> child.getExtensionFor(new MeterRegisterChangeRequestCustomPropertySet()))
                        .flatMap(Functions.asStream())
                        .map(MeterRegisterChangeRequestDomainExtension::getErrorMessage)
                        .filter(Objects::nonNull)
                        .findFirst();
                if (errorMessage.isPresent()) {
                    confirmationMessage.setLog(subParent.getState() == DefaultState.PARTIAL_SUCCESS ? createPartiallySuccessfulLog(errorMessage.get()) : createFailedLog(errorMessage.get()));
                } else {
                    confirmationMessage.setLog(subParent.getState() == DefaultState.PARTIAL_SUCCESS ? createPartiallySuccessfulLog() : createFailedLog());
                }
            }
        }

        private UtilsDvceERPSmrtMtrRegChgConfUtilsDvce createChildBody(String sapDeviceId) {
            UtilitiesDeviceID deviceId = objectFactory.createUtilitiesDeviceID();
            deviceId.setValue(sapDeviceId);

            UtilsDvceERPSmrtMtrRegChgConfUtilsDvce device = objectFactory.createUtilsDvceERPSmrtMtrRegChgConfUtilsDvce();
            device.setID(deviceId);

            return device;
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

        private BusinessDocumentMessageID createID(String id) {
            BusinessDocumentMessageID messageID = objectFactory.createBusinessDocumentMessageID();
            messageID.setValue(id);
            return messageID;
        }

        private com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementconfirmation.UUID createUUID(String uuid) {
            com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementconfirmation.UUID messageUUID
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

        private Log createPartiallySuccessfulLog() {
            Log log = objectFactory.createLog();
            log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.PARTIALLY_SUCCESSFUL.getCode());
            return log;
        }

        private Log createPartiallySuccessfulLog(String message) {
            Log log = objectFactory.createLog();
            log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.PARTIALLY_SUCCESSFUL.getCode());
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
    }
}
