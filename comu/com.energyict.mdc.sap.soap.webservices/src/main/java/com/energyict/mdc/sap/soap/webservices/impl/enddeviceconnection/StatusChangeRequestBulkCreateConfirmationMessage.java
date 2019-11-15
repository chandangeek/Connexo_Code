/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.ProcessingResultCode;
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

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.PROCESSING_ERROR_CATEGORY_CODE;

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

        public Builder from(ServiceCall subParent, List<ServiceCall> children, Instant now) {
            ServiceCall parent = subParent.getParent().orElseThrow(() -> new IllegalStateException("Unable to get parent for service call"));
            MasterConnectionStatusChangeDomainExtension extension = parent.getExtension(MasterConnectionStatusChangeDomainExtension.class)
                    .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));

            confirmationMessage.setMessageHeader(createHeader(extension.getRequestID(), extension.getUuid(), now));
            confirmationMessage.getSmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationMessage().add(createBody(subParent, children));
            return this;
        }

        public Builder from(StatusChangeRequestBulkCreateMessage message, String exceptionID, String exceptionMessage, Instant now) {
            confirmationMessage.setMessageHeader(createHeader(message.getId(), message.getUuid(), now));
            confirmationMessage.getSmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationMessage().addAll(createBodies(message));
            confirmationMessage.setLog(createLog(exceptionID, PROCESSING_ERROR_CATEGORY_CODE, exceptionMessage));
            return this;
        }

        public Builder from(StatusChangeRequestCreateMessage message, String exceptionID, String exceptionMessage, Instant now) {
            confirmationMessage.setMessageHeader(createHeader(message.getId(), message.getUuid(), now));
            SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg msg = createBody(message);
            msg.setLog(createLog(exceptionID, PROCESSING_ERROR_CATEGORY_CODE, exceptionMessage));
            confirmationMessage.getSmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationMessage().add(msg);
            return this;
        }

        public Builder withSingleStatus(String messageId, String deviceID, ProcessingResultCode processingResultCode, Instant processDate) {
            if (messageId != null) {
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

                confirmationMessage.getSmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationMessage()
                        .stream().map(r -> r.getUtilitiesConnectionStatusChangeRequest())
                        .filter(s -> s.getID() != null)
                        .filter(s -> messageId.equals(s.getID().getValue()))
                        .forEach(c -> c.getDeviceConnectionStatus().add(deviceConnectionStatus));
            }
            return this;
        }

        private BusinessDocumentMessageHeader createHeader(String parentId, String referenceUuid, Instant now) {
            BusinessDocumentMessageHeader header = OBJECT_FACTORY.createBusinessDocumentMessageHeader();
            header.setCreationDateTime(now);
            header.setReferenceID(createID(parentId));
            header.setReferenceUUID(createUUID(referenceUuid));

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

        private SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg createBody(ServiceCall parent, List<ServiceCall> children) {
            SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg messageBody = createBaseBody();

            ConnectionStatusChangeDomainExtension extension = parent.getExtension(ConnectionStatusChangeDomainExtension.class)
                    .orElseThrow(() -> new IllegalStateException("Unable to get domain extension for service call"));
            messageBody.getUtilitiesConnectionStatusChangeRequest().getID().setValue(extension.getId());
            messageBody.getUtilitiesConnectionStatusChangeRequest().getCategoryCode().setValue(extension.getCategoryCode());
            children.stream().forEach(serviceCall -> messageBody.getUtilitiesConnectionStatusChangeRequest().getDeviceConnectionStatus()
                    .add(createDeviceConnectionStatus(serviceCall)));
            return messageBody;
        }

        private List<SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg> createBodies(StatusChangeRequestBulkCreateMessage message) {
            return message.getRequests().stream()
                    .map(m -> {
                        SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg messageBody = createBaseBody();

                        messageBody.getUtilitiesConnectionStatusChangeRequest().getID().setValue(m.getId());
                        messageBody.getUtilitiesConnectionStatusChangeRequest().getCategoryCode().setValue(m.getCategoryCode());
                        return messageBody;
                    }).collect(Collectors.toList());
        }

        private SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg createBody(StatusChangeRequestCreateMessage message) {
            SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg messageBody = createBaseBody();

            messageBody.getUtilitiesConnectionStatusChangeRequest().getID().setValue(message.getId());
            messageBody.getUtilitiesConnectionStatusChangeRequest().getCategoryCode().setValue(message.getCategoryCode());

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
                    ? ProcessingResultCode.SUCCESSFUL.getCode()
                    : ProcessingResultCode.FAILED.getCode());
            deviceConnectionStatus.setUtilitiesDeviceConnectionStatusProcessingResultCode(resultCode);

            return deviceConnectionStatus;
        }

        private Log createLog(String id, String categoryCode, String message) {
            LogItemCategoryCode logItemCategoryCode = OBJECT_FACTORY.createLogItemCategoryCode();
            logItemCategoryCode.setValue(categoryCode);

            LogItem logItem = OBJECT_FACTORY.createLogItem();
            logItem.setTypeID(id);
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
        public StatusChangeRequestBulkCreateConfirmationMessage build() {
            return StatusChangeRequestBulkCreateConfirmationMessage.this;
        }
    }
}
