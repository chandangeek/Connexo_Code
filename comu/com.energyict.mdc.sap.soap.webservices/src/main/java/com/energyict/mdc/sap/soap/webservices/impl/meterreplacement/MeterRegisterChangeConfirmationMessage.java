/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.meterreplacement;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.ProcessingResultCode;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.MasterMeterRegisterChangeRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.MasterMeterRegisterChangeRequestDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.MeterRegisterChangeRequestCustomPropertySet;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement.MeterRegisterChangeRequestDomainExtension;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacement.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacement.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacement.Log;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacement.LogItem;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacement.LogItemCategoryCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacement.ObjectFactory;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacement.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacement.UtilsDvceERPSmrtMtrRegChgConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacement.UtilsDvceERPSmrtMtrRegChgConfUtilsDvce;

import java.time.Instant;
import java.util.UUID;

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
            confirmationMessage.setMessageHeader(createMessageHeader(extension.getRequestId(), now));

            if (parent.getState().equals(DefaultState.CANCELLED)) {
                confirmationMessage.setLog(createFailedLog(String.valueOf(MessageSeeds.SERVICE_CALL_WAS_CANCELLED.getNumber()), MessageSeeds.SERVICE_CALL_WAS_CANCELLED.getDefaultFormat(null)));
            } else if (parent.getState().equals(DefaultState.SUCCESSFUL)) {
                confirmationMessage.setLog(createSuccessfulLog());
            } else {
                confirmationMessage.setLog(createFailedLog());
            }

            createBody(confirmationMessage, child, now);
            return this;
        }

        public MeterRegisterChangeConfirmationMessage.Builder from(MeterRegisterChangeMessage message, MessageSeeds messageSeed, Instant now) {
            confirmationMessage = objectFactory.createUtilsDvceERPSmrtMtrRegChgConfMsg();
            confirmationMessage.setMessageHeader(createMessageHeader(message.getId(), now));

            confirmationMessage.setLog(createFailedLog(String.valueOf(messageSeed.getNumber()), messageSeed.getDefaultFormat(null)));
            return this;
        }

        public MeterRegisterChangeConfirmationMessage build() {
            return MeterRegisterChangeConfirmationMessage.this;
        }

        private void createBody(UtilsDvceERPSmrtMtrRegChgConfMsg confirmationMessage, ServiceCall child, Instant now) {
            MeterRegisterChangeRequestDomainExtension extension = child.getExtensionFor(new MeterRegisterChangeRequestCustomPropertySet()).get();

            confirmationMessage.setMessageHeader(createMessageHeader(extension.getRequestId(), now));
            confirmationMessage.setUtilitiesDevice(createChildBody(extension.getDeviceId()));
            if (child.getState() == DefaultState.SUCCESSFUL) {
                confirmationMessage.setLog(createSuccessfulLog());
            } else if (child.getState() == DefaultState.FAILED || child.getState() == DefaultState.CANCELLED) {
                confirmationMessage.setLog(createFailedLog(extension.getErrorCode(), extension.getErrorMessage()));
            }
        }

        private UtilsDvceERPSmrtMtrRegChgConfUtilsDvce createChildBody(String sapDeviceId) {
            UtilitiesDeviceID deviceId = objectFactory.createUtilitiesDeviceID();
            deviceId.setValue(sapDeviceId);

            UtilsDvceERPSmrtMtrRegChgConfUtilsDvce device = objectFactory.createUtilsDvceERPSmrtMtrRegChgConfUtilsDvce();
            device.setID(deviceId);

            return device;
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

        private com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacement.UUID createUUID(String uuid) {
            com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacement.UUID messageUUID
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
