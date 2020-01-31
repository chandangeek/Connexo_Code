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
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementconfirmation.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementconfirmation.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementconfirmation.Log;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementconfirmation.LogItem;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementconfirmation.LogItemCategoryCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementconfirmation.ObjectFactory;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementconfirmation.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementconfirmation.UtilsDvceERPSmrtMtrRegChgConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementconfirmation.UtilsDvceERPSmrtMtrRegChgConfUtilsDvce;

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

        public MeterRegisterChangeConfirmationMessage.Builder from(ServiceCall parent, String senderBusinessSystemId, Instant now) {
            MasterMeterRegisterChangeRequestDomainExtension extension = parent.getExtensionFor(new MasterMeterRegisterChangeRequestCustomPropertySet()).get();
            ServiceCall child = parent.findChildren().stream().findFirst().orElseThrow(() -> new IllegalStateException("Unable to get child service call"));
            confirmationMessage = objectFactory.createUtilsDvceERPSmrtMtrRegChgConfMsg();
            confirmationMessage.setMessageHeader(createMessageHeader(extension.getRequestId(), extension.getUuid(), senderBusinessSystemId, now));

            if (parent.getState().equals(DefaultState.CANCELLED)) {
                confirmationMessage.setLog(createFailedLog(MessageSeeds.REQUEST_CANCELLED.getDefaultFormat()));
            } else if (parent.getState().equals(DefaultState.SUCCESSFUL)) {
                confirmationMessage.setLog(createSuccessfulLog());
            } else if (parent.getState().equals(DefaultState.PARTIAL_SUCCESS)) {
                confirmationMessage.setLog(createPartiallySuccessfulLog());
            } else {
                confirmationMessage.setLog(createFailedLog(MessageSeeds.REQUEST_WAS_FAILED.getDefaultFormat()));
            }

            createBody(confirmationMessage, child, senderBusinessSystemId, now);
            return this;
        }

        public MeterRegisterChangeConfirmationMessage.Builder from(MeterRegisterChangeMessage message, MessageSeeds messageSeed, String senderBusinessSystemId, Instant now, Object ...messageSeedArgs) {
            confirmationMessage = objectFactory.createUtilsDvceERPSmrtMtrRegChgConfMsg();
            confirmationMessage.setMessageHeader(createMessageHeader(message.getId(), message.getUuid(), senderBusinessSystemId, now));
            confirmationMessage.setUtilitiesDevice(createChildBody(message.getDeviceId()));
            confirmationMessage.setLog(createFailedLog(messageSeed.getDefaultFormat(messageSeedArgs)));
            return this;
        }

        public MeterRegisterChangeConfirmationMessage build() {
            return MeterRegisterChangeConfirmationMessage.this;
        }

        private void createBody(UtilsDvceERPSmrtMtrRegChgConfMsg confirmationMessage, ServiceCall subParent, String senderBusinessSystemId, Instant now) {
            SubMasterMeterRegisterChangeRequestDomainExtension extension = subParent.getExtensionFor(new SubMasterMeterRegisterChangeRequestCustomPropertySet())
                    .orElseThrow(() -> new IllegalStateException("Can not find domain extension for service call"));

            confirmationMessage.setMessageHeader(createMessageHeader(extension.getRequestId(), extension.getUuid(), senderBusinessSystemId, now));
            confirmationMessage.setUtilitiesDevice(createChildBody(extension.getDeviceId()));
            if (subParent.getState() == DefaultState.SUCCESSFUL) {
                confirmationMessage.setLog(createSuccessfulLog());
            } else if (subParent.getState() == DefaultState.FAILED || subParent.getState() == DefaultState.PARTIAL_SUCCESS || subParent.getState() == DefaultState.CANCELLED) {
                List<String> errorMessages = ServiceCallHelper.findChildren(subParent).stream()
                        .map(child -> child.getExtensionFor(new MeterRegisterChangeRequestCustomPropertySet()))
                        .flatMap(Functions.asStream())
                        .map(MeterRegisterChangeRequestDomainExtension::getErrorMessage)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                if (!errorMessages.isEmpty()) {
                    confirmationMessage.setLog(subParent.getState() == DefaultState.PARTIAL_SUCCESS ? createPartiallySuccessfulLog(errorMessages) : createFailedLog(errorMessages));
                } else {
                    confirmationMessage.setLog(subParent.getState() == DefaultState.PARTIAL_SUCCESS ? createPartiallySuccessfulLog() : createFailedLog(MessageSeeds.REQUEST_WAS_FAILED.getDefaultFormat()));
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

        private com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementconfirmation.UUID createUUID(String uuid) {
            com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementconfirmation.UUID messageUUID
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

        private Log createFailedLog(String message) {
            Log log = objectFactory.createLog();
            log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.FAILED.getCode());
            log.getItem().add(createLogItem(message, UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID,
                    SeverityCode.ERROR.getCode(), PROCESSING_ERROR_CATEGORY_CODE));
            setMaximumLogItemSeverityCode(log);
            return log;
        }

        private Log createFailedLog(List<String> messages) {
            Log log = objectFactory.createLog();
            log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.FAILED.getCode());
            messages.stream().forEach(message -> log.getItem().add(createLogItem(message, UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID,
                    SeverityCode.ERROR.getCode(), PROCESSING_ERROR_CATEGORY_CODE)));
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
            messages.stream().forEach(message -> log.getItem().add(createLogItem(message, UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID,
                    SeverityCode.ERROR.getCode(), PROCESSING_ERROR_CATEGORY_CODE)));
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
