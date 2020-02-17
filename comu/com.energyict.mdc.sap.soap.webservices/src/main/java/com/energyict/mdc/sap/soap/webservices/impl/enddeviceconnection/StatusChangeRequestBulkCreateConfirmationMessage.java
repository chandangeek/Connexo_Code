/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
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
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection.MasterConnectionStatusChangeDomainExtension;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreateconfirmation.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreateconfirmation.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreateconfirmation.Log;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreateconfirmation.LogItem;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreateconfirmation.LogItemCategoryCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreateconfirmation.ObjectFactory;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreateconfirmation.SmrtMtrUtilsConncnStsChgReqERPBulkCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreateconfirmation.SmrtMtrUtilsConncnStsChgReqERPCrteConfDvceConncnSts;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreateconfirmation.SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreateconfirmation.SmrtMtrUtilsConncnStsChgReqERPCrteConfUtilsConncnStsChgReq;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreateconfirmation.UtilitiesConnectionStatusChangeRequestCategoryCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreateconfirmation.UtilitiesConnectionStatusChangeRequestID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreateconfirmation.UtilitiesConnectionStatusChangeResultCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreateconfirmation.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreateconfirmation.UUID;

import com.google.common.base.Strings;

import java.time.Instant;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.PROCESSING_ERROR_CATEGORY_CODE;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.SUCCESSFUL_PROCESSING_TYPE_ID;
import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID;

public class StatusChangeRequestBulkCreateConfirmationMessage {

    private final ObjectFactory OBJECT_FACTORY = new ObjectFactory();
    private final SAPCustomPropertySets sapCustomPropertySets;

    private SmrtMtrUtilsConncnStsChgReqERPBulkCrteConfMsg confirmationMessage;

    private StatusChangeRequestBulkCreateConfirmationMessage(SAPCustomPropertySets sapCustomPropertySets) {
        this.sapCustomPropertySets = sapCustomPropertySets;
    }

    public SmrtMtrUtilsConncnStsChgReqERPBulkCrteConfMsg getConfirmationMessage() {
        return confirmationMessage;
    }

    public static Builder builder(SAPCustomPropertySets sapCustomPropertySets) {
        return new StatusChangeRequestBulkCreateConfirmationMessage(sapCustomPropertySets).new Builder();
    }

    public class Builder {

        private Builder() {
            confirmationMessage = OBJECT_FACTORY.createSmrtMtrUtilsConncnStsChgReqERPBulkCrteConfMsg();
        }

        public Builder from(ServiceCall subParent, List<ServiceCall> children, String senderBusinessSystemId, Instant now) {
            ServiceCall parent = subParent.getParent().orElseThrow(() -> new IllegalStateException("Unable to get parent for service call"));
            MasterConnectionStatusChangeDomainExtension extension = parent.getExtension(MasterConnectionStatusChangeDomainExtension.class)
                    .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));

            confirmationMessage.setMessageHeader(createHeader(extension.getRequestID(), extension.getUuid(), senderBusinessSystemId, now));
            confirmationMessage.getSmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationMessage().add(createBody(subParent, children, senderBusinessSystemId, now));
            // parent state still not changed before sending confirmation, so check children states
            if (ServiceCallHelper.hasAllChildrenInState(children, DefaultState.SUCCESSFUL)) {
                confirmationMessage.setLog(createSuccessfulLog());
            } else if (ServiceCallHelper.hasAllChildrenInState(children, DefaultState.CANCELLED)) {
                confirmationMessage.setLog(createFailedLog(MessageSeeds.REQUEST_CANCELLED.getDefaultFormat()));
            } else if (ServiceCallHelper.hasAnyChildState(children, DefaultState.SUCCESSFUL)) {
                confirmationMessage.setLog(createPartiallySuccessfulLog());
            } else {
                confirmationMessage.setLog(createFailedLog(MessageSeeds.BULK_REQUEST_WAS_FAILED.getDefaultFormat()));
            }
            return this;
        }

        public Builder from(StatusChangeRequestBulkCreateMessage messages, String exceptionMessage, String senderBusinessSystemId, Instant now) {
            confirmationMessage.setMessageHeader(createHeader(messages.getId(), messages.getUuid(), senderBusinessSystemId, now));
            confirmationMessage.getSmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationMessage().addAll(createFailedBodies(messages, senderBusinessSystemId, now));
            confirmationMessage.setLog(createFailedLog(exceptionMessage));
            return this;
        }

        public Builder from(StatusChangeRequestBulkCreateMessage messages, StatusChangeRequestCreateMessage message, String exceptionMessage, String senderBusinessSystemId, Instant now) {
            confirmationMessage.setMessageHeader(createHeader(messages.getId(), messages.getUuid(), senderBusinessSystemId, now));
            SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg msg = createFailedBody(message, senderBusinessSystemId, now);
            msg.setLog(createFailedLog(exceptionMessage));
            confirmationMessage.getSmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationMessage().add(msg);
            confirmationMessage.setLog(createFailedLog(exceptionMessage));
            return this;
        }

        public Builder withSingleStatus(String messageId, String deviceID, ConnectionStatusProcessingResultCode processingResultCode, Instant processDate) {
            if (messageId != null) {
                SmrtMtrUtilsConncnStsChgReqERPCrteConfDvceConncnSts deviceConnectionStatus = createConnectionStatus(deviceID, processingResultCode, processDate);
                confirmationMessage.getSmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationMessage()
                        .stream().map(r -> r.getUtilitiesConnectionStatusChangeRequest())
                        .filter(s -> s.getID() != null)
                        .filter(s -> messageId.equals(s.getID().getValue()))
                        .forEach(c -> {
                            c.getDeviceConnectionStatus().clear(); // clear all previously added device connection statuses, we need only on with deviceId
                            c.getDeviceConnectionStatus().add(deviceConnectionStatus);
                        });
            }
            return this;
        }

        private BusinessDocumentMessageHeader createHeader(String parentId, String referenceUuid, String senderBusinessSystemId, Instant now) {
            BusinessDocumentMessageHeader header = OBJECT_FACTORY.createBusinessDocumentMessageHeader();
            header.setCreationDateTime(now);
            header.setUUID(createUUID(java.util.UUID.randomUUID().toString()));
            if (!Strings.isNullOrEmpty(parentId)){
                header.setReferenceID(createID(parentId));
            }
            if (!Strings.isNullOrEmpty(referenceUuid)){
                header.setReferenceUUID(createUUID(referenceUuid));
            }
            header.setSenderBusinessSystemID(senderBusinessSystemId);
            header.setReconciliationIndicator(true);
            return header;
        }

        private SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg createBaseBody() {
            UtilitiesConnectionStatusChangeRequestID messageID =
                    OBJECT_FACTORY.createUtilitiesConnectionStatusChangeRequestID();
            UtilitiesConnectionStatusChangeRequestCategoryCode messageCategoryCode =
                    OBJECT_FACTORY.createUtilitiesConnectionStatusChangeRequestCategoryCode();
            SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg messageBody =
                    OBJECT_FACTORY.createSmrtMtrUtilsConncnStsChgReqERPCrteConfMsg();
            SmrtMtrUtilsConncnStsChgReqERPCrteConfUtilsConncnStsChgReq request = OBJECT_FACTORY.createSmrtMtrUtilsConncnStsChgReqERPCrteConfUtilsConncnStsChgReq();

            request.setID(messageID);
            request.setCategoryCode(messageCategoryCode);
            messageBody.setUtilitiesConnectionStatusChangeRequest(request);

            return messageBody;
        }

        private SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg createBody(ServiceCall parent, List<ServiceCall> children, String senderBusinessSystemId, Instant now) {
            SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg messageBody = createBaseBody();

            ConnectionStatusChangeDomainExtension extension = parent.getExtension(ConnectionStatusChangeDomainExtension.class)
                    .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));
            messageBody.setMessageHeader(createHeader(extension.getRequestId(), extension.getUuid(), senderBusinessSystemId, now));
            messageBody.getUtilitiesConnectionStatusChangeRequest().getID().setValue(extension.getId());
            messageBody.getUtilitiesConnectionStatusChangeRequest().getCategoryCode().setValue(extension.getCategoryCode());
            children.stream().forEach(serviceCall -> messageBody.getUtilitiesConnectionStatusChangeRequest().getDeviceConnectionStatus()
                    .add(createDeviceConnectionStatus(serviceCall)));
            // parent state still not changed before sending confirmation, so check children states
            if (ServiceCallHelper.hasAllChildrenInState(children, DefaultState.SUCCESSFUL)) {
                messageBody.setLog(createSuccessfulLog());
            } else if (ServiceCallHelper.hasAllChildrenInState(children, DefaultState.CANCELLED)) {
                messageBody.setLog(createFailedLog(MessageSeeds.REQUEST_CANCELLED.getDefaultFormat()));
            } else if (ServiceCallHelper.hasAnyChildState(children, DefaultState.SUCCESSFUL)) {
                messageBody.setLog(createPartiallySuccessfulLog());
            } else {
                messageBody.setLog(createFailedLog(MessageSeeds.BULK_REQUEST_WAS_FAILED.getDefaultFormat()));
            }
            return messageBody;
        }

        private List<SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg> createFailedBodies(StatusChangeRequestBulkCreateMessage message, String senderBusinessSystemId, Instant now) {
            return message.getRequests().stream()
                    .map(m -> {
                        SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg messageBody = createBaseBody();
                        messageBody.setMessageHeader(createHeader(m.getRequestId(), m.getUuid(), senderBusinessSystemId, now));
                        messageBody.getUtilitiesConnectionStatusChangeRequest().getID().setValue(m.getId());
                        messageBody.getUtilitiesConnectionStatusChangeRequest().getCategoryCode().setValue(m.getCategoryCode());
                        messageBody.setLog(createFailedLog(MessageSeeds.REQUEST_WAS_FAILED.getDefaultFormat()));
                        messageBody.getUtilitiesConnectionStatusChangeRequest().getDeviceConnectionStatus()
                                .addAll(createFailedDeviceConnectionStatuses(m, now));
                        return messageBody;
                    }).collect(Collectors.toList());
        }

        private SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg createFailedBody(StatusChangeRequestCreateMessage message, String senderBusinessSystemId, Instant now) {
            SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg messageBody = createBaseBody();

            messageBody.setMessageHeader(createHeader(message.getRequestId(), message.getUuid(), senderBusinessSystemId, now));
            messageBody.getUtilitiesConnectionStatusChangeRequest().getID().setValue(message.getId());
            messageBody.getUtilitiesConnectionStatusChangeRequest().getCategoryCode().setValue(message.getCategoryCode());
            messageBody.getUtilitiesConnectionStatusChangeRequest().getDeviceConnectionStatus()
                    .addAll(createFailedDeviceConnectionStatuses(message, now));
            return messageBody;
        }

        private SmrtMtrUtilsConncnStsChgReqERPCrteConfDvceConncnSts createDeviceConnectionStatus(ServiceCall serviceCall) {
            SmrtMtrUtilsConncnStsChgReqERPCrteConfDvceConncnSts deviceConnectionStatus =
                    OBJECT_FACTORY.createSmrtMtrUtilsConncnStsChgReqERPCrteConfDvceConncnSts();
            deviceConnectionStatus.setProcessingDateTime(serviceCall.getLastModificationTime());

            UtilitiesDeviceID deviceID = OBJECT_FACTORY.createUtilitiesDeviceID();
            serviceCall.getTargetObject().ifPresent(id -> {
                if (id instanceof Device) {
                    sapCustomPropertySets.getSapDeviceId((Device) id)
                            .ifPresent(sapId -> deviceID.setValue(sapId));
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

        public StatusChangeRequestBulkCreateConfirmationMessage build() {
            return StatusChangeRequestBulkCreateConfirmationMessage.this;
        }
    }
}
