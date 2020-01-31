/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.meterreplacement;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.util.streams.Predicates;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.ProcessingResultCode;
import com.energyict.mdc.sap.soap.webservices.impl.SeverityCode;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallHelper;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.MasterMeterRegisterChangeRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.MasterMeterRegisterChangeRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.MeterRegisterChangeRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.MeterRegisterChangeRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.SubMasterMeterRegisterChangeRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.SubMasterMeterRegisterChangeRequestDomainExtension;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkconfirmation.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkconfirmation.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkconfirmation.Log;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkconfirmation.LogItem;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkconfirmation.LogItemCategoryCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkconfirmation.ObjectFactory;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkconfirmation.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkconfirmation.UtilsDvceERPSmrtMtrRegBulkChgConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkconfirmation.UtilsDvceERPSmrtMtrRegChgConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkconfirmation.UtilsDvceERPSmrtMtrRegChgConfUtilsDvce;

import com.google.common.base.Strings;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.PROCESSING_ERROR_CATEGORY_CODE;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.SUCCESSFUL_PROCESSING_TYPE_ID;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID;

public class MeterRegisterBulkChangeConfirmationMessage {
    private final ObjectFactory objectFactory = new ObjectFactory();
    private UtilsDvceERPSmrtMtrRegBulkChgConfMsg confirmationMessage;

    public UtilsDvceERPSmrtMtrRegBulkChgConfMsg getConfirmationMessage() {
        return confirmationMessage;
    }

    public static Builder builder() {
        return new MeterRegisterBulkChangeConfirmationMessage().new Builder();
    }

    public class Builder {

        private Builder() {
        }

        public Builder from(ServiceCall parent, String senderBusinessSystemId, Instant now) {
            List<ServiceCall> children = ServiceCallHelper.findChildren(parent);
            MasterMeterRegisterChangeRequestDomainExtension extension = parent.getExtensionFor(new MasterMeterRegisterChangeRequestCustomPropertySet()).get();
            confirmationMessage = objectFactory.createUtilsDvceERPSmrtMtrRegBulkChgConfMsg();
            confirmationMessage.setMessageHeader(createMessageHeader(extension.getRequestId(), extension.getUuid(), senderBusinessSystemId, now));

            if (parent.getState().equals(DefaultState.CANCELLED)) {
                confirmationMessage.setLog(createFailedLog(MessageSeeds.REQUEST_CANCELLED.getDefaultFormat()));
            } else if (parent.getState().equals(DefaultState.SUCCESSFUL)) {
                confirmationMessage.setLog(createSuccessfulLog());
            } else if (parent.getState().equals(DefaultState.PARTIAL_SUCCESS)) {
                confirmationMessage.setLog(createPartiallySuccessfulLog());
            } else {
                confirmationMessage.setLog(createFailedLog(MessageSeeds.BULK_REQUEST_WAS_FAILED.getDefaultFormat()));
            }

            createBody(confirmationMessage, children, senderBusinessSystemId, now);
            return this;
        }

        public Builder from(MeterRegisterBulkChangeRequestMessage messages, MeterRegisterChangeMessage message, MessageSeeds messageSeed, String senderBusinessSystemId, Instant now, Object ...messageSeedArgs) {
            confirmationMessage = objectFactory.createUtilsDvceERPSmrtMtrRegBulkChgConfMsg();
            confirmationMessage.setMessageHeader(createMessageHeader(messages.getRequestId(), messages.getUuid(), senderBusinessSystemId, now));
            UtilsDvceERPSmrtMtrRegChgConfMsg confMsg = objectFactory.createUtilsDvceERPSmrtMtrRegChgConfMsg();
            confMsg.setUtilitiesDevice(createChildBody(message.getDeviceId()));

            confirmationMessage.getUtilitiesDeviceERPSmartMeterRegisterChangeConfirmationMessage().add(confMsg);
            confirmationMessage.setLog(createFailedLog(messageSeed.getDefaultFormat(messageSeedArgs)));
            return this;
        }

        public Builder from(MeterRegisterBulkChangeRequestMessage messages, MessageSeeds messageSeed, String senderBusinessSystemId, Instant now, Object ...messageSeedArgs) {
            confirmationMessage = objectFactory.createUtilsDvceERPSmrtMtrRegBulkChgConfMsg();
            confirmationMessage.setMessageHeader(createMessageHeader(messages.getRequestId(), messages.getUuid(), senderBusinessSystemId, now));

            createBody(confirmationMessage, messages, senderBusinessSystemId, now);
            confirmationMessage.setLog(createFailedLog(messageSeed.getDefaultFormat(messageSeedArgs)));
            return this;
        }

        public MeterRegisterBulkChangeConfirmationMessage build() {
            return MeterRegisterBulkChangeConfirmationMessage.this;
        }

        private void createBody(UtilsDvceERPSmrtMtrRegBulkChgConfMsg confirmationMessage,
                                List<ServiceCall> subParents, String senderBusinessSystemId, Instant now) {

            subParents.forEach(subParent -> {
                confirmationMessage.getUtilitiesDeviceERPSmartMeterRegisterChangeConfirmationMessage()
                        .add(createChildMessage(subParent, senderBusinessSystemId, now));
            });
        }

        private void createBody(UtilsDvceERPSmrtMtrRegBulkChgConfMsg confirmationMessage, MeterRegisterBulkChangeRequestMessage messages, String senderBusinessSystemId, Instant now) {
            messages.getMeterRegisterChangeMessages().forEach(message -> {
                UtilsDvceERPSmrtMtrRegChgConfMsg confMessage = objectFactory.createUtilsDvceERPSmrtMtrRegChgConfMsg();
                confMessage.setMessageHeader(createChildHeader(message.getId(), message.getUuid(), senderBusinessSystemId, now));
                confMessage.setUtilitiesDevice(createChildBody(message.getDeviceId()));
                confirmationMessage.getUtilitiesDeviceERPSmartMeterRegisterChangeConfirmationMessage().add(confMessage);
            });
        }

        private UtilsDvceERPSmrtMtrRegChgConfMsg createChildMessage(ServiceCall subParentServiceCall, String senderBusinessSystemId, Instant now) {
            SubMasterMeterRegisterChangeRequestDomainExtension extension = subParentServiceCall.getExtensionFor(new SubMasterMeterRegisterChangeRequestCustomPropertySet())
                    .orElseThrow(() -> new IllegalStateException("Can not find domain extension for service call"));
            UtilsDvceERPSmrtMtrRegChgConfMsg confirmationMessage = objectFactory.createUtilsDvceERPSmrtMtrRegChgConfMsg();
            confirmationMessage.setMessageHeader(createChildHeader(extension.getRequestId(), extension.getUuid(), senderBusinessSystemId, now));
            confirmationMessage.setUtilitiesDevice(createChildBody(extension.getDeviceId()));
            if (subParentServiceCall.getState() == DefaultState.SUCCESSFUL) {
                confirmationMessage.setLog(createSuccessfulLog());
            } else if (subParentServiceCall.getState() == DefaultState.FAILED || subParentServiceCall.getState() == DefaultState.PARTIAL_SUCCESS || subParentServiceCall.getState() == DefaultState.CANCELLED) {
                List<String> errorMessages = ServiceCallHelper.findChildren(subParentServiceCall).stream()
                        .map(child -> child.getExtensionFor(new MeterRegisterChangeRequestCustomPropertySet()))
                        .flatMap(Functions.asStream())
                        .map(MeterRegisterChangeRequestDomainExtension::getErrorMessage)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                if (!errorMessages.isEmpty()) {
                    confirmationMessage.setLog(subParentServiceCall.getState() == DefaultState.PARTIAL_SUCCESS ? createPartiallySuccessfulLog(errorMessages) : createFailedLog(errorMessages));
                } else {
                    confirmationMessage.setLog(subParentServiceCall.getState() == DefaultState.PARTIAL_SUCCESS ? createPartiallySuccessfulLog() : createFailedLog(MessageSeeds.BULK_REQUEST_WAS_FAILED.getDefaultFormat()));
                }
            }
            return confirmationMessage;
        }

        private UtilsDvceERPSmrtMtrRegChgConfUtilsDvce createChildBody(String sapDeviceId) {
            UtilitiesDeviceID deviceId = objectFactory.createUtilitiesDeviceID();
            deviceId.setValue(sapDeviceId);

            UtilsDvceERPSmrtMtrRegChgConfUtilsDvce device = objectFactory.createUtilsDvceERPSmrtMtrRegChgConfUtilsDvce();
            device.setID(deviceId);

            return device;
        }

        private BusinessDocumentMessageHeader createChildHeader(String id, String uuid, String senderBusinessSystemId, Instant now) {
            BusinessDocumentMessageHeader header = objectFactory.createBusinessDocumentMessageHeader();
            if (!Strings.isNullOrEmpty(id)) {
                header.setReferenceID(createID(id));
            }
            if (!Strings.isNullOrEmpty(uuid)){
                header.setReferenceUUID(createUUID(uuid));
            }
            header.setSenderBusinessSystemID(senderBusinessSystemId);
            header.setReconciliationIndicator(true);
            header.setCreationDateTime(now);
            return header;
        }

        private BusinessDocumentMessageHeader createMessageHeader(String requestId, String referenceUuid, String senderBusinessSystemId, Instant now) {
            String uuid = UUID.randomUUID().toString();

            BusinessDocumentMessageHeader header = objectFactory.createBusinessDocumentMessageHeader();
            if (!Strings.isNullOrEmpty(requestId)) {
                header.setReferenceID(createID(requestId));
            }
            header.setUUID(createUUID(uuid));
            if (!Strings.isNullOrEmpty(referenceUuid)) {
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

        private com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkconfirmation.UUID createUUID(String uuid) {
            com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkconfirmation.UUID messageUUID
                    = objectFactory.createUUID();
            messageUUID.setValue(uuid);
            return messageUUID;
        }

        private Log createSuccessfulLog() {
            Log log = objectFactory.createLog();
            log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.SUCCESSFUL.getCode());
            log.getItem().add(createLogItem(MessageSeeds.OK_RESULT.getDefaultFormat(),
                    SUCCESSFUL_PROCESSING_TYPE_ID, SeverityCode.INFORMATION.getCode(),
                    null));
            setMaximumLogItemSeverityCode(log);
            return log;
        }

        private Log createPartiallySuccessfulLog() {
            Log log = objectFactory.createLog();
            log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.PARTIALLY_SUCCESSFUL.getCode());
            log.getItem().add(createLogItem(MessageSeeds.PARTIALLY_SUCCESSFUL.getDefaultFormat(),
                    UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID, SeverityCode.ERROR.getCode(),
                    null));
            setMaximumLogItemSeverityCode(log);
            return log;
        }

        private Log createPartiallySuccessfulLog(List<String> messages) {
            Log log = objectFactory.createLog();
            log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.PARTIALLY_SUCCESSFUL.getCode());
            messages.stream().forEach(message -> log.getItem().add(createLogItem(message,
                    UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID, SeverityCode.ERROR.getCode(),
                    null)));
            setMaximumLogItemSeverityCode(log);
            return log;
        }

        private Log createFailedLog(List<String> messages) {
            Log log = objectFactory.createLog();
            log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.FAILED.getCode());
            messages.stream().forEach(message -> log.getItem().add(createLogItem(message, UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID,
                    SeverityCode.ERROR.getCode(), PROCESSING_ERROR_CATEGORY_CODE)));
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
}
