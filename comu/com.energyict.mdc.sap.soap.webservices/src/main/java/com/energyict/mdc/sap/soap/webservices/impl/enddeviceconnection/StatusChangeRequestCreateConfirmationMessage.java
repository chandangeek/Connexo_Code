/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.util.streams.Predicates;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.ProcessingResultCode;
import com.energyict.mdc.sap.soap.webservices.impl.SeverityCode;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.ServiceCallHelper;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection.ConnectionStatusChangeDomainExtension;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreateconfirmation.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreateconfirmation.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreateconfirmation.Log;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreateconfirmation.LogItem;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreateconfirmation.LogItemCategoryCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreateconfirmation.ObjectFactory;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreateconfirmation.SmrtMtrUtilsConncnStsChgReqERPCrteConfDvceConncnSts;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreateconfirmation.SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreateconfirmation.SmrtMtrUtilsConncnStsChgReqERPCrteConfUtilsConncnStsChgReq;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreateconfirmation.UtilitiesConnectionStatusChangeRequestCategoryCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreateconfirmation.UtilitiesConnectionStatusChangeRequestID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreateconfirmation.UtilitiesConnectionStatusChangeResultCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreateconfirmation.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreateconfirmation.UUID;

import com.google.common.base.Strings;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.PROCESSING_ERROR_CATEGORY_CODE;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.SUCCESSFUL_PROCESSING_TYPE_ID;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID;

public class StatusChangeRequestCreateConfirmationMessage {

    private final ObjectFactory OBJECT_FACTORY = new ObjectFactory();
    private final Optional<SAPCustomPropertySets> sapCustomPropertySets;

    private SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg confirmationMessage;

    private StatusChangeRequestCreateConfirmationMessage() {
        this.sapCustomPropertySets = Optional.empty();
    }

    private StatusChangeRequestCreateConfirmationMessage(SAPCustomPropertySets sapCustomPropertySets) {
        this.sapCustomPropertySets = Optional.ofNullable(sapCustomPropertySets);
    }

    public SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg getConfirmationMessage() {
        return confirmationMessage;
    }

    public static Builder builder() {
        return new StatusChangeRequestCreateConfirmationMessage().new Builder();
    }

    public static Builder builder(SAPCustomPropertySets sapCustomPropertySets) {
        return new StatusChangeRequestCreateConfirmationMessage(sapCustomPropertySets).new Builder();
    }

    public class Builder {

        private Builder() {
            confirmationMessage = OBJECT_FACTORY.createSmrtMtrUtilsConncnStsChgReqERPCrteConfMsg();
        }

        public Builder from(ServiceCall parent, List<ServiceCall> children, String senderBusinessSystemId, Instant now) {
            ConnectionStatusChangeDomainExtension extension = parent.getExtension(ConnectionStatusChangeDomainExtension.class)
                    .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));
            confirmationMessage.setMessageHeader(createHeader(extension.getRequestId(), extension.getUuid(), senderBusinessSystemId, now));
            confirmationMessage.setUtilitiesConnectionStatusChangeRequest(createBody(parent, children));
            // parent state still not changed before sending confirmation, so check children states
            if (ServiceCallHelper.hasAllChildrenInState(children, DefaultState.SUCCESSFUL)) {
                confirmationMessage.setLog(createSuccessfulLog());
            } else if (ServiceCallHelper.hasAllChildrenInState(children, DefaultState.CANCELLED)) {
                confirmationMessage.setLog(createFailedLog(MessageSeeds.SERVICE_CALL_WAS_CANCELLED.getDefaultFormat()));
            } else if (ServiceCallHelper.hasAnyChildState(children, DefaultState.SUCCESSFUL)) {
                confirmationMessage.setLog(createPartiallySuccessfulLog());
            } else {
                confirmationMessage.setLog(createFailedLog(MessageSeeds.REQUEST_WAS_FAILED.getDefaultFormat()));
            }
            return this;
        }

        public Builder from(StatusChangeRequestCreateMessage message, String exceptionMessage, String senderBusinessSystemId, Instant now) {
            confirmationMessage.setMessageHeader(createHeader(message.getRequestId(), message.getUuid(), senderBusinessSystemId, now));
            confirmationMessage.setUtilitiesConnectionStatusChangeRequest(createFailedBody(message, now));
            confirmationMessage.setLog(createFailedLog(exceptionMessage));
            return this;
        }

        public Builder withStatus(String deviceID, ConnectionStatusProcessingResultCode processingResultCode, Instant processDate) {
            SmrtMtrUtilsConncnStsChgReqERPCrteConfDvceConncnSts deviceConnectionStatus = createConnectionStatus(deviceID, processingResultCode, processDate);
            confirmationMessage.getUtilitiesConnectionStatusChangeRequest()
                    .getDeviceConnectionStatus().clear(); // clear all previously added device connection statuses, we need only on with deviceId
            confirmationMessage.getUtilitiesConnectionStatusChangeRequest()
                    .getDeviceConnectionStatus()
                    .add(deviceConnectionStatus);

            return this;
        }

        private BusinessDocumentMessageHeader createHeader(String requestId, String uuid, String senderBusinessSystemId, Instant now) {
            BusinessDocumentMessageHeader header = OBJECT_FACTORY.createBusinessDocumentMessageHeader();

            header.setUUID(createUUID(java.util.UUID.randomUUID().toString()));
            if (!Strings.isNullOrEmpty(requestId)) {
                header.setReferenceID(createID(requestId));
            }
            if (!Strings.isNullOrEmpty(uuid)) {
                header.setReferenceUUID(createUUID(uuid));
            }
            header.setSenderBusinessSystemID(senderBusinessSystemId);
            header.setReconciliationIndicator(true);
            header.setCreationDateTime(now);

            return header;
        }

        private SmrtMtrUtilsConncnStsChgReqERPCrteConfUtilsConncnStsChgReq createBaseBody() {
            UtilitiesConnectionStatusChangeRequestID messageID =
                    OBJECT_FACTORY.createUtilitiesConnectionStatusChangeRequestID();
            UtilitiesConnectionStatusChangeRequestCategoryCode messageCategoryCode =
                    OBJECT_FACTORY.createUtilitiesConnectionStatusChangeRequestCategoryCode();
            SmrtMtrUtilsConncnStsChgReqERPCrteConfUtilsConncnStsChgReq messageBody =
                    OBJECT_FACTORY.createSmrtMtrUtilsConncnStsChgReqERPCrteConfUtilsConncnStsChgReq();

            messageBody.setID(messageID);
            messageBody.setCategoryCode(messageCategoryCode);

            return messageBody;
        }

        private SmrtMtrUtilsConncnStsChgReqERPCrteConfUtilsConncnStsChgReq createBody(ServiceCall parent,
                                                                                      List<ServiceCall> childs) {
            SmrtMtrUtilsConncnStsChgReqERPCrteConfUtilsConncnStsChgReq messageBody = createBaseBody();

            parent.getExtension(ConnectionStatusChangeDomainExtension.class).ifPresent(domainExtension -> {
                messageBody.getID().setValue(domainExtension.getId());
                messageBody.getCategoryCode().setValue(domainExtension.getCategoryCode());
            });

            childs.forEach(serviceCall -> messageBody.getDeviceConnectionStatus()
                    .add(createDeviceConnectionStatus(serviceCall)));

            return messageBody;
        }

        private SmrtMtrUtilsConncnStsChgReqERPCrteConfUtilsConncnStsChgReq createFailedBody(StatusChangeRequestCreateMessage message, Instant now) {
            SmrtMtrUtilsConncnStsChgReqERPCrteConfUtilsConncnStsChgReq messageBody = createBaseBody();

            messageBody.getID().setValue(message.getId());
            messageBody.getCategoryCode().setValue(message.getCategoryCode());

            messageBody.getDeviceConnectionStatus().addAll(createFailedDeviceConnectionStatuses(message, now));

            return messageBody;
        }

        private SmrtMtrUtilsConncnStsChgReqERPCrteConfDvceConncnSts createDeviceConnectionStatus(ServiceCall serviceCall) {
            SmrtMtrUtilsConncnStsChgReqERPCrteConfDvceConncnSts deviceConnectionStatus =
                    OBJECT_FACTORY.createSmrtMtrUtilsConncnStsChgReqERPCrteConfDvceConncnSts();
            deviceConnectionStatus.setProcessingDateTime(serviceCall.getLastModificationTime());

            UtilitiesDeviceID deviceID = OBJECT_FACTORY.createUtilitiesDeviceID();
            serviceCall.getTargetObject().ifPresent(id -> {
                if (id instanceof Device) {
                    sapCustomPropertySets.ifPresent(cps -> cps.getSapDeviceId((Device) id)
                            .ifPresent(deviceID::setValue));
                    deviceConnectionStatus.setUtilitiesDeviceID(deviceID);
                }
            });

            UtilitiesConnectionStatusChangeResultCode resultCode =
                    OBJECT_FACTORY.createUtilitiesConnectionStatusChangeResultCode();
            resultCode.setValue(serviceCall.getState().equals(DefaultState.SUCCESSFUL)
                    ? ConnectionStatusProcessingResultCode.SUCCESSFUL.getCode()
                    : ConnectionStatusProcessingResultCode.FAILED.getCode());
            deviceConnectionStatus.setUtilitiesDeviceConnectionStatusProcessingResultCode(resultCode);

            return deviceConnectionStatus;
        }

        private List<SmrtMtrUtilsConncnStsChgReqERPCrteConfDvceConncnSts> createFailedDeviceConnectionStatuses(StatusChangeRequestCreateMessage message, Instant now) {
            return message.getDeviceConnectionStatus().keySet().stream().map(sapDeviceId -> createConnectionStatus(sapDeviceId, ConnectionStatusProcessingResultCode.FAILED, now)).collect(Collectors.toList());
        }

        SmrtMtrUtilsConncnStsChgReqERPCrteConfDvceConncnSts createConnectionStatus(String sapDeviceId, ConnectionStatusProcessingResultCode processingResutCode, Instant now) {
            SmrtMtrUtilsConncnStsChgReqERPCrteConfDvceConncnSts deviceConnectionStatus =
                    OBJECT_FACTORY.createSmrtMtrUtilsConncnStsChgReqERPCrteConfDvceConncnSts();
            deviceConnectionStatus.setProcessingDateTime(now);

            UtilitiesDeviceID deviceID = OBJECT_FACTORY.createUtilitiesDeviceID();
            deviceID.setValue(sapDeviceId);
            deviceConnectionStatus.setUtilitiesDeviceID(deviceID);

            UtilitiesConnectionStatusChangeResultCode resultCode =
                    OBJECT_FACTORY.createUtilitiesConnectionStatusChangeResultCode();
            resultCode.setValue(processingResutCode.getCode());
            deviceConnectionStatus.setUtilitiesDeviceConnectionStatusProcessingResultCode(resultCode);
            return deviceConnectionStatus;
        }

        private BusinessDocumentMessageID createID(String id) {
            BusinessDocumentMessageID messageID = OBJECT_FACTORY.createBusinessDocumentMessageID();
            messageID.setValue(id);
            return messageID;
        }

        private UUID createUUID(String uuid) {
            UUID messageUUID = OBJECT_FACTORY.createUUID();
            messageUUID.setValue(uuid);
            return messageUUID;
        }

        private Log createSuccessfulLog() {
            Log log = OBJECT_FACTORY.createLog();
            log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.SUCCESSFUL.getCode());
            log.getItem().add(createLogItem(MessageSeeds.OK_RESULT.getDefaultFormat(),
                    SUCCESSFUL_PROCESSING_TYPE_ID, SeverityCode.INFORMATION.getCode(),
                    null));
            setMaximumLogItemSeverityCode(log);
            return log;
        }

        private Log createFailedLog(String message) {
            Log log = OBJECT_FACTORY.createLog();
            log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.FAILED.getCode());
            log.getItem().add(createLogItem(message, UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID,
                    SeverityCode.ERROR.getCode(), PROCESSING_ERROR_CATEGORY_CODE));
            setMaximumLogItemSeverityCode(log);
            return log;
        }

        private Log createPartiallySuccessfulLog() {
            Log log = OBJECT_FACTORY.createLog();
            log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.PARTIALLY_SUCCESSFUL.getCode());
            log.getItem().add(createLogItem(MessageSeeds.PARTIALLY_SUCCESSFUL.getDefaultFormat(),
                    UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID, SeverityCode.ERROR.getCode(),
                    null));
            setMaximumLogItemSeverityCode(log);
            return log;
        }

        private LogItem createLogItem(String message, String typeId, String severityCode, String categoryCode) {
            LogItem logItem = OBJECT_FACTORY.createLogItem();
            if (!Strings.isNullOrEmpty(categoryCode)) {
                LogItemCategoryCode logItemCategoryCode = OBJECT_FACTORY.createLogItemCategoryCode();
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

        public StatusChangeRequestCreateConfirmationMessage build() {
            return StatusChangeRequestCreateConfirmationMessage.this;
        }
    }
}
