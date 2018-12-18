/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.servicecall.enddeviceconnection.ConnectionStatusChangeDomainExtension;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreateconfirmation.BusinessDocumentMessageHeader;
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

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class StatusChangeRequestCreateConfirmationMessage {

    private final ObjectFactory OBJECT_FACTORY = new ObjectFactory();
    private final Optional<SAPCustomPropertySets> sapCustomPropertySets;

    private SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg confirmationMessage;
    private String url;

    private StatusChangeRequestCreateConfirmationMessage() {
        this.sapCustomPropertySets = Optional.empty();
    }

    private StatusChangeRequestCreateConfirmationMessage(SAPCustomPropertySets sapCustomPropertySets) {
        this.sapCustomPropertySets = Optional.ofNullable(sapCustomPropertySets);
    }

    public SmrtMtrUtilsConncnStsChgReqERPCrteConfMsg getConfirmationMessage() {
        return confirmationMessage;
    }

    public String getUrl() {
        return url;
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
            confirmationMessage.setMessageHeader(createHeader());
        }

        public Builder from(ServiceCall parent, List<ServiceCall> childs) {
            confirmationMessage.setUtilitiesConnectionStatusChangeRequest(createBody(parent, childs));
            return this;
        }

        public Builder from(StatusChangeRequestCreateMessage message, String exceptionID, String exceptionMessage) {
            confirmationMessage.setUtilitiesConnectionStatusChangeRequest(createBody(message));
            confirmationMessage.setLog(createLog(exceptionID, "PRE", exceptionMessage));
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

        private BusinessDocumentMessageHeader createHeader() {
            return OBJECT_FACTORY.createBusinessDocumentMessageHeader();
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
                StatusChangeRequestCreateConfirmationMessage.this.url = domainExtension.getConfirmationURL();
            });

            childs.forEach(serviceCall -> messageBody.getDeviceConnectionStatus()
                    .add(createDeviceConnectionStatus(serviceCall)));

            return messageBody;
        }

        private SmrtMtrUtilsConncnStsChgReqERPCrteConfUtilsConncnStsChgReq createBody(StatusChangeRequestCreateMessage message) {
            SmrtMtrUtilsConncnStsChgReqERPCrteConfUtilsConncnStsChgReq messageBody = createBaseBody();

            messageBody.getID().setValue(message.getId());
            messageBody.getCategoryCode().setValue(message.getCategoryCode());
            StatusChangeRequestCreateConfirmationMessage.this.url = message.getConfirmationEndpointURL();

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
                            .ifPresent(sapId -> deviceID.setValue(sapId.toPlainString())));
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

        public StatusChangeRequestCreateConfirmationMessage build() {
            return StatusChangeRequestCreateConfirmationMessage.this;
        }
    }
}
