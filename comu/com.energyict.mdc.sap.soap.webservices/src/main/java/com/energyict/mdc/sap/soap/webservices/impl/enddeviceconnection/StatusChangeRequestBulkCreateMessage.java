/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreate.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreate.SmrtMtrUtilsConncnStsChgReqERPBulkCrteReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreate.SmrtMtrUtilsConncnStsChgReqERPCrteReqDvceConncnSts;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreate.SmrtMtrUtilsConncnStsChgReqERPCrteReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreate.SmrtMtrUtilsConncnStsChgReqERPCrteReqSmrtMtr;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreate.SmrtMtrUtilsConncnStsChgReqERPCrteReqUtilsConncnStsChgReq;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreate.UtilitiesAdvancedMeteringSystemID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreate.UtilitiesConnectionStatusChangeRequestCategoryCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreate.UtilitiesConnectionStatusChangeRequestID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestbulkcreate.UtilitiesDeviceID;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class StatusChangeRequestBulkCreateMessage {

    private String id;

    private List<StatusChangeRequestCreateMessage> requests = new ArrayList<>();

    private StatusChangeRequestBulkCreateMessage() {
    }

    public static StatusChangeRequestBulkCreateMessage.Builder builder() {
        return new StatusChangeRequestBulkCreateMessage().new Builder();
    }

    public String getId() {
        return id;
    }

    public List<StatusChangeRequestCreateMessage> getRequests() {
        return requests;
    }

    public boolean isValid() {
        return id != null && !requests.isEmpty();
    }

    public class Builder {

        private Builder() {
        }

        public StatusChangeRequestBulkCreateMessage.Builder from(SmrtMtrUtilsConncnStsChgReqERPBulkCrteReqMsg requestMessage) {
            setId(getRequestId(requestMessage));
            Optional.ofNullable(requestMessage.getSmartMeterUtilitiesConnectionStatusChangeRequestERPCreateRequestMessage())
                    .ifPresent(statusChangeRequest -> {
                        setRequestMessages(getRequestMessages(statusChangeRequest));
                    });
            return this;
        }

        public Builder setRequestMessages(List<StatusChangeRequestCreateMessage> requestMessages) {
            StatusChangeRequestBulkCreateMessage.this.requests.addAll(requestMessages);
            return this;
        }

        public Builder setId(String id) {
            StatusChangeRequestBulkCreateMessage.this.id = id;
            return this;
        }

        private List<StatusChangeRequestCreateMessage> getRequestMessages(List<SmrtMtrUtilsConncnStsChgReqERPCrteReqMsg> statusChangeRequest) {
            return statusChangeRequest.stream().map(m -> getRequestMessage(m)).collect(Collectors.toList());
        }

        private StatusChangeRequestCreateMessage getRequestMessage(SmrtMtrUtilsConncnStsChgReqERPCrteReqMsg msg) {
            return new StatusChangeRequestCreateMessage(getId(msg.getUtilitiesConnectionStatusChangeRequest()),
                    getCategoryCode(msg.getUtilitiesConnectionStatusChangeRequest()),
                    getUtilitiesServiceDisconnectionReasonCode(msg.getUtilitiesConnectionStatusChangeRequest()),
                    msg.getUtilitiesConnectionStatusChangeRequest().getPlannedProcessingDateTime(),
                    getDeviceConnectionStatus(msg.getUtilitiesConnectionStatusChangeRequest()),
                    true);
        }

        public StatusChangeRequestBulkCreateMessage build() {
            return StatusChangeRequestBulkCreateMessage.this;
        }

        private String getRequestId(SmrtMtrUtilsConncnStsChgReqERPBulkCrteReqMsg changeRequest) {
            return Optional.ofNullable(changeRequest.getMessageHeader().getID())
                    .map(BusinessDocumentMessageID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getId(SmrtMtrUtilsConncnStsChgReqERPCrteReqUtilsConncnStsChgReq changeRequest) {
            return Optional.ofNullable(changeRequest.getID())
                    .map(UtilitiesConnectionStatusChangeRequestID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getCategoryCode(SmrtMtrUtilsConncnStsChgReqERPCrteReqUtilsConncnStsChgReq changeRequest) {
            return Optional.ofNullable(changeRequest.getCategoryCode())
                    .map(UtilitiesConnectionStatusChangeRequestCategoryCode::getValue)
                    .filter(categoryCode -> !Checks.is(categoryCode).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private Map<String, String> getDeviceConnectionStatus(SmrtMtrUtilsConncnStsChgReqERPCrteReqUtilsConncnStsChgReq changeRequest) {
            return Optional.ofNullable(changeRequest)
                    .map(SmrtMtrUtilsConncnStsChgReqERPCrteReqUtilsConncnStsChgReq::getDeviceConnectionStatus)
                    .orElse(new ArrayList<>())
                    .stream()
                    .collect(Collectors.toMap(this::getUtilitiesDeviceID, this::getUtilitiesAdvancedMeteringSystemID, (oldValue, newValue) -> oldValue));
        }

        private String getUtilitiesAdvancedMeteringSystemID(SmrtMtrUtilsConncnStsChgReqERPCrteReqDvceConncnSts deviceConnectionStatus) {
            return Optional.ofNullable(deviceConnectionStatus.getSmartMeter())
                    .map(SmrtMtrUtilsConncnStsChgReqERPCrteReqSmrtMtr::getUtilitiesAdvancedMeteringSystemID)
                    .map(UtilitiesAdvancedMeteringSystemID::getValue)
                    .filter(amsID -> !Checks.is(amsID).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getUtilitiesDeviceID(SmrtMtrUtilsConncnStsChgReqERPCrteReqDvceConncnSts deviceConnectionStatus) {
            return Optional.ofNullable(deviceConnectionStatus.getUtilitiesDeviceID())
                    .map(UtilitiesDeviceID::getValue)
                    .filter(sapDeviceID -> !Checks.is(sapDeviceID).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getUtilitiesServiceDisconnectionReasonCode(SmrtMtrUtilsConncnStsChgReqERPCrteReqUtilsConncnStsChgReq changeRequest) {
            return Optional.ofNullable(changeRequest.getUtilitiesServiceDisconnectionReasonCode())
                    .filter(sapDeviceID -> !Checks.is(sapDeviceID).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }
    }
}
