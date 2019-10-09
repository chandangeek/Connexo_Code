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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator.PROCESSING_ERROR_CATEGORY_CODE;

public class StatusChangeRequestBulkCreateConfirmationMessage {

    private final ObjectFactory OBJECT_FACTORY = new ObjectFactory();
    private final Optional<SAPCustomPropertySets> sapCustomPropertySets;

    private SmrtMtrUtilsConncnStsChgReqERPBulkCrteConfMsg confirmationMessage;

    private StatusChangeRequestBulkCreateConfirmationMessage() {
        this.sapCustomPropertySets = Optional.empty();
    }

    private StatusChangeRequestBulkCreateConfirmationMessage(SAPCustomPropertySets sapCustomPropertySets) {
        this.sapCustomPropertySets = Optional.ofNullable(sapCustomPropertySets);
    }

    public SmrtMtrUtilsConncnStsChgReqERPBulkCrteConfMsg getConfirmationMessage() {
        return confirmationMessage;
    }

    public static Builder builder() {
        return new StatusChangeRequestBulkCreateConfirmationMessage().new Builder();
    }

    public static Builder builder(SAPCustomPropertySets sapCustomPropertySets) {
        return new StatusChangeRequestBulkCreateConfirmationMessage(sapCustomPropertySets).new Builder();
    }

    public class Builder {

        private Builder() {
            confirmationMessage = OBJECT_FACTORY.createSmrtMtrUtilsConncnStsChgReqERPBulkCrteConfMsg();
        }

        public Builder from(ServiceCall subParent, List<ServiceCall> childs, Instant now) {
            confirmationMessage.setMessageHeader(createHeader(now));
            confirmationMessage.getSmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationMessage().add(createBody(subParent, childs));
            return this;
        }

        public Builder from(StatusChangeRequestBulkCreateMessage message, String exceptionID, String exceptionMessage, Instant now) {
            confirmationMessage.setMessageHeader(createHeader(now));
            confirmationMessage.getSmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationMessage().addAll(createBody(message));
            confirmationMessage.setLog(createLog(exceptionID, PROCESSING_ERROR_CATEGORY_CODE, exceptionMessage));
            return this;
        }

        public Builder from(StatusChangeRequestCreateMessage message, String exceptionID, String exceptionMessage, Instant now) {
            confirmationMessage.setMessageHeader(createHeader(now));
            SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg msg = createBody(message);
            msg.setLog(createLog(exceptionID, PROCESSING_ERROR_CATEGORY_CODE, exceptionMessage));
            confirmationMessage.getSmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationMessage().add(msg);
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

            confirmationMessage.getSmartMeterUtilitiesConnectionStatusChangeRequestERPCreateConfirmationMessage().forEach(m -> m.getUtilitiesConnectionStatusChangeRequest().getDeviceConnectionStatus().add(deviceConnectionStatus));

            return this;
        }

        private BusinessDocumentMessageHeader createHeader(ServiceCall parent, Instant now) {
            BusinessDocumentMessageHeader header = OBJECT_FACTORY.createBusinessDocumentMessageHeader();
            header.setCreationDateTime(now);
            parent.getExtension(MasterConnectionStatusChangeDomainExtension.class).ifPresent(ext -> {
                BusinessDocumentMessageID id = OBJECT_FACTORY.createBusinessDocumentMessageID();
                id.setValue(ext.getRequestID());
                header.setID(id);
            });

            return header;
        }

        private BusinessDocumentMessageHeader createHeader(Instant now) {
            BusinessDocumentMessageHeader header = OBJECT_FACTORY.createBusinessDocumentMessageHeader();
            header.setCreationDateTime(now);

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

        private SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg createBody(ServiceCall parent, List<ServiceCall> childs) {
            SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg messageBody = createBaseBody();

            parent.getExtension(ConnectionStatusChangeDomainExtension.class).ifPresent(domainExtension -> {
                messageBody.getUtilitiesConnectionStatusChangeRequest().getID().setValue(domainExtension.getId());
                messageBody.getUtilitiesConnectionStatusChangeRequest().getCategoryCode().setValue(domainExtension.getCategoryCode());
            });
            childs.stream().forEach(serviceCall -> messageBody.getUtilitiesConnectionStatusChangeRequest().getDeviceConnectionStatus()
                    .add(createDeviceConnectionStatus(serviceCall)));
            return messageBody;
        }

        private List<SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg> createBody(StatusChangeRequestBulkCreateMessage message) {
            List<SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg> list = new ArrayList<>();
            message.getRequests().forEach(r -> {
                SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg messageBody = createBaseBody();

                messageBody.getUtilitiesConnectionStatusChangeRequest().getID().setValue(r.getId());
                messageBody.getUtilitiesConnectionStatusChangeRequest().getCategoryCode().setValue(r.getCategoryCode());

                list.add(messageBody);
            });
            return list;
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
                    sapCustomPropertySets.ifPresent(cps -> cps.getSapDeviceId(((Device) id).getName())
                            .ifPresent(sapId -> deviceID.setValue(sapId)));
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

        public StatusChangeRequestBulkCreateConfirmationMessage build() {
            return StatusChangeRequestBulkCreateConfirmationMessage.this;
        }
    }
}
