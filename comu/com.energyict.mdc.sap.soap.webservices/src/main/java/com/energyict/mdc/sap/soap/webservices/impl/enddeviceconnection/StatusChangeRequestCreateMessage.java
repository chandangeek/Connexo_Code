/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreate.SmrtMtrUtilsConncnStsChgReqERPCrteReqDvceConncnSts;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreate.SmrtMtrUtilsConncnStsChgReqERPCrteReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreate.SmrtMtrUtilsConncnStsChgReqERPCrteReqSmrtMtr;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreate.SmrtMtrUtilsConncnStsChgReqERPCrteReqUtilsConncnStsChgReq;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreate.UtilitiesAdvancedMeteringSystemID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreate.UtilitiesConnectionStatusChangeRequestCategoryCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreate.UtilitiesConnectionStatusChangeRequestID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreate.UtilitiesDeviceID;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class StatusChangeRequestCreateMessage {

    private Instant plannedProcessingDateTime;
    private Map<String, String> deviceConnectionStatus;
    private String id;
    private String categoryCode;
    private String utilitiesServiceDisconnectionReasonCode;

    private StatusChangeRequestCreateMessage() {
    }

    public String getId() {
        return id;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public Map<String, String> getDeviceConnectionStatus() {
        return deviceConnectionStatus;
    }

    public Instant getPlannedProcessingDateTime() {
        return plannedProcessingDateTime;
    }

    public String getUtilitiesServiceDisconnectionReasonCode() {
        return utilitiesServiceDisconnectionReasonCode;
    }

    public boolean isValid() {
        return id != null && categoryCode != null && plannedProcessingDateTime != null &&
               !deviceConnectionStatus.isEmpty();
    }

    public static Builder builder() {
        return new StatusChangeRequestCreateMessage().new Builder();
    }

    public class Builder {

        private Builder() {
        }

        public Builder from(SmrtMtrUtilsConncnStsChgReqERPCrteReqMsg requestMessage) {
            Optional.ofNullable(requestMessage.getUtilitiesConnectionStatusChangeRequest())
                    .ifPresent(statusChangeRequest -> {
                        setId(getId(statusChangeRequest));
                        setCategoryCode(getCategoryCode(statusChangeRequest));
                        setUtilitiesServiceDisconnectionReasonCode(getUtilitiesServiceDisconnectionReasonCode(statusChangeRequest));
                        setPlannedProcessingDateTime(statusChangeRequest.getPlannedProcessingDateTime());
                        setDeviceConnectionStatus(getDeviceConnectionStatus(statusChangeRequest));
                    });
            return this;
        }

        public Builder setId(String id) {
            StatusChangeRequestCreateMessage.this.id = id;
            return this;
        }

        public Builder setCategoryCode(String categoryCode) {
            StatusChangeRequestCreateMessage.this.categoryCode = categoryCode;
            return this;
        }

        public Builder setDeviceConnectionStatus(Map<String, String> deviceConnectionStatus) {
            StatusChangeRequestCreateMessage.this.deviceConnectionStatus = deviceConnectionStatus;
            return this;
        }

        public Builder setPlannedProcessingDateTime(Instant plannedProcessingDateTime) {
            StatusChangeRequestCreateMessage.this.plannedProcessingDateTime = plannedProcessingDateTime;
            return this;
        }

        public Builder setUtilitiesServiceDisconnectionReasonCode(String utilitiesServiceDisconnectionReasonCode) {
            StatusChangeRequestCreateMessage.this.utilitiesServiceDisconnectionReasonCode = utilitiesServiceDisconnectionReasonCode;
            return this;
        }

        public StatusChangeRequestCreateMessage build() {
            return StatusChangeRequestCreateMessage.this;
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