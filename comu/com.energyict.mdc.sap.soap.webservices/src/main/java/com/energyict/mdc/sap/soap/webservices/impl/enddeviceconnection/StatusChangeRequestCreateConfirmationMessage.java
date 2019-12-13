/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.ProcessingResultCode;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;
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

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.PROCESSING_ERROR_CATEGORY_CODE;
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

        public Builder from(ServiceCall parent, List<ServiceCall> children, Instant now) {
            ConnectionStatusChangeDomainExtension extension = parent.getExtension(ConnectionStatusChangeDomainExtension.class)
                    .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));
            confirmationMessage.setMessageHeader(createHeader(extension.getRequestId(), extension.getUuid(), now));
            confirmationMessage.setUtilitiesConnectionStatusChangeRequest(createBody(parent, children));
            // parent state still not changed before sending confirmation, so check children states
            if (ServiceCallHelper.hasAllChildrenInState(children, DefaultState.SUCCESSFUL)) {
                confirmationMessage.setLog(createSuccessfulLog());
            } else if (ServiceCallHelper.hasAllChildrenInState(children, DefaultState.CANCELLED)) {
                confirmationMessage.setLog(createFailedLog(MessageSeeds.SERVICE_CALL_WAS_CANCELLED.getDefaultFormat()));
            } else if (ServiceCallHelper.hasAnyChildState(children, DefaultState.SUCCESSFUL)) {
                confirmationMessage.setLog(createPartiallySuccessfulLog());
            } else {
                confirmationMessage.setLog(createFailedLog());
            }
            return this;
        }

        public Builder from(StatusChangeRequestCreateMessage message, String exceptionMessage, Instant now) {
            confirmationMessage.setMessageHeader(createHeader(message.getRequestId(), message.getUuid(), now));
            confirmationMessage.setUtilitiesConnectionStatusChangeRequest(createBody(message));
            confirmationMessage.setLog(createLog(PROCESSING_ERROR_CATEGORY_CODE, exceptionMessage));
            return this;
        }

        public Builder withStatus(String deviceID, ProcessingResultCode processingResultCode, Instant processDate) {
            UtilitiesDeviceID id = OBJECT_FACTORY.createUtilitiesDeviceID();
            UtilitiesConnectionStatusChangeResultCode resultCode =
                    OBJECT_FACTORY.createUtilitiesConnectionStatusChangeResultCode();
            SmrtMtrUtilsConncnStsChgReqERPCrteConfDvceConncnSts deviceConnectionStatus =
                    OBJECT_FACTORY.createSmrtMtrUtilsConncnStsChgReqERPCrteConfDvceConncnSts();

            id.setValue(deviceID);
            resultCode.setValue(processingResultCode.getCode());

            deviceConnectionStatus.setUtilitiesDeviceID(id);
            deviceConnectionStatus.setUtilitiesDeviceConnectionStatusProcessingResultCode(resultCode);
            deviceConnectionStatus.setProcessingDateTime(processDate);

            confirmationMessage.getUtilitiesConnectionStatusChangeRequest()
                    .getDeviceConnectionStatus()
                    .add(deviceConnectionStatus);

            return this;
        }

        private BusinessDocumentMessageHeader createHeader(String requestId, String uuid, Instant now) {
            BusinessDocumentMessageHeader header = OBJECT_FACTORY.createBusinessDocumentMessageHeader();

            if (!Strings.isNullOrEmpty(requestId)) {
                header.setReferenceID(createID(requestId));
            }
            if (!Strings.isNullOrEmpty(uuid)) {
                header.setReferenceUUID(createUUID(uuid));
            }
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

        private SmrtMtrUtilsConncnStsChgReqERPCrteConfUtilsConncnStsChgReq createBody(StatusChangeRequestCreateMessage message) {
            SmrtMtrUtilsConncnStsChgReqERPCrteConfUtilsConncnStsChgReq messageBody = createBaseBody();

            messageBody.getID().setValue(message.getId());
            messageBody.getCategoryCode().setValue(message.getCategoryCode());

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

        private Log createLog(String categoryCode, String message) {
            LogItemCategoryCode logItemCategoryCode = OBJECT_FACTORY.createLogItemCategoryCode();
            logItemCategoryCode.setValue(categoryCode);

            LogItem logItem = OBJECT_FACTORY.createLogItem();
            logItem.setTypeID(UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID);
            logItem.setCategoryCode(logItemCategoryCode);
            logItem.setNote(message);

            Log log = OBJECT_FACTORY.createLog();
            log.getItem().add(logItem);

            return log;
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
            return log;
        }

        private Log createFailedLog() {
            Log log = OBJECT_FACTORY.createLog();
            log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.FAILED.getCode());
            return log;
        }

        private Log createFailedLog(String message) {
            Log log = OBJECT_FACTORY.createLog();
            log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.FAILED.getCode());
            log.getItem().add(createLogItem(message));
            return log;
        }

        private Log createPartiallySuccessfulLog() {
            Log log = OBJECT_FACTORY.createLog();
            log.setBusinessDocumentProcessingResultCode(ProcessingResultCode.PARTIALLY_SUCCESSFUL.getCode());
            return log;
        }

        private LogItem createLogItem(String message) {
            LogItemCategoryCode logItemCategoryCode = OBJECT_FACTORY.createLogItemCategoryCode();
            logItemCategoryCode.setValue(WebServiceActivator.PROCESSING_ERROR_CATEGORY_CODE);

            LogItem logItem = OBJECT_FACTORY.createLogItem();
            logItem.setTypeID(UNSUCCESSFUL_PROCESSING_ERROR_TYPE_ID);
            logItem.setCategoryCode(logItemCategoryCode);
            logItem.setNote(message);

            return logItem;
        }

        public StatusChangeRequestCreateConfirmationMessage build() {
            return StatusChangeRequestCreateConfirmationMessage.this;
        }
    }
}
