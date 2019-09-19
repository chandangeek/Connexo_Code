/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.meterreplacement;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.ProcessingResultCode;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallHelper;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.MasterMeterRegisterChangeRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.MasterMeterRegisterChangeRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.MeterRegisterChangeRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.MeterRegisterChangeRequestDomainExtension;
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

import java.time.Instant;
import java.util.List;
import java.util.UUID;

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

        public Builder from(ServiceCall parent, Instant now) {
            List<ServiceCall> children = ServiceCallHelper.findChildren(parent);
            MasterMeterRegisterChangeRequestDomainExtension extension = parent.getExtensionFor(new MasterMeterRegisterChangeRequestCustomPropertySet()).get();
            confirmationMessage = objectFactory.createUtilsDvceERPSmrtMtrRegBulkChgConfMsg();
            confirmationMessage.setMessageHeader(createMessageHeader(extension.getRequestId(), now));

            if (parent.getState().equals(DefaultState.CANCELLED)) {
                confirmationMessage.setLog(createFailedLog(String.valueOf(MessageSeeds.SERVICE_CALL_WAS_CANCELLED.getNumber()), MessageSeeds.SERVICE_CALL_WAS_CANCELLED.getDefaultFormat(null)));
            } else if (parent.getState().equals(DefaultState.SUCCESSFUL)) {
                confirmationMessage.setLog(createSuccessfulLog());
            } else if (parent.getState().equals(DefaultState.PARTIAL_SUCCESS)) {
                confirmationMessage.setLog(createPartiallySuccessfulLog());
            } else {
                confirmationMessage.setLog(createFailedLog());
            }

            createBody(confirmationMessage, children, now);
            return this;
        }

        public Builder from(MeterRegisterBulkChangeRequestMessage message, MessageSeeds messageSeed, Instant now) {
            confirmationMessage = objectFactory.createUtilsDvceERPSmrtMtrRegBulkChgConfMsg();
            confirmationMessage.setMessageHeader(createMessageHeader(message.getRequestId(), now));

            confirmationMessage.setLog(createFailedLog(String.valueOf(messageSeed.getNumber()), messageSeed.getDefaultFormat(null)));
            return this;
        }

        public MeterRegisterBulkChangeConfirmationMessage build() {
            return MeterRegisterBulkChangeConfirmationMessage.this;
        }

        private void createBody(UtilsDvceERPSmrtMtrRegBulkChgConfMsg confirmationMessage,
                                List<ServiceCall> children, Instant now) {

            children.forEach(child -> {
                confirmationMessage.getUtilitiesDeviceERPSmartMeterRegisterChangeConfirmationMessage()
                        .add(createChildMessage(child, now));
            });
        }

        private UtilsDvceERPSmrtMtrRegChgConfMsg createChildMessage(ServiceCall childServiceCall, Instant now) {
            MeterRegisterChangeRequestDomainExtension extension = childServiceCall.getExtensionFor(new MeterRegisterChangeRequestCustomPropertySet()).get();

            UtilsDvceERPSmrtMtrRegChgConfMsg confirmationMessage = objectFactory.createUtilsDvceERPSmrtMtrRegChgConfMsg();
            confirmationMessage.setMessageHeader(createChildHeader(now));
            confirmationMessage.setUtilitiesDevice(createChildBody(extension.getDeviceId()));
            if (childServiceCall.getState() == DefaultState.SUCCESSFUL) {
                confirmationMessage.setLog(createSuccessfulLog());
            } else if (childServiceCall.getState() == DefaultState.FAILED || childServiceCall.getState() == DefaultState.CANCELLED) {
                confirmationMessage.setLog(createFailedLog(extension.getErrorCode(), extension.getErrorMessage()));
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

        private com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkconfirmation.UUID createUUID(String uuid) {
            com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkconfirmation.UUID messageUUID
                    = objectFactory.createUUID();
            messageUUID.setValue(uuid);
            return messageUUID;
        }

        private Log createSuccessfulLog() {
            Log log = objectFactory.createLog();
            log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.SUCCESSFUL.getCode());
            return log;
        }

        private Log createPartiallySuccessfulLog() {
            Log log = objectFactory.createLog();
            log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.PARTIALLY_SUCCESSFUL.getCode());
            return log;
        }

        private Log createFailedLog() {
            Log log = objectFactory.createLog();
            log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.FAILED.getCode());
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
            logItemCategoryCode.setValue("PRE");

            LogItem logItem = objectFactory.createLogItem();
            logItem.setTypeID(code);
            logItem.setCategoryCode(logItemCategoryCode);
            logItem.setNote(message);

            return logItem;
        }
    }
}
