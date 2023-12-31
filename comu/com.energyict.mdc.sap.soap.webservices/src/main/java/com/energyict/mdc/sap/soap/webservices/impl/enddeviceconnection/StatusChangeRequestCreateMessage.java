/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.sap.soap.webservices.impl.AbstractSapMessage;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreate.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreate.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreate.SmrtMtrUtilsConncnStsChgReqERPCrteReqDvceConncnSts;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreate.SmrtMtrUtilsConncnStsChgReqERPCrteReqMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreate.SmrtMtrUtilsConncnStsChgReqERPCrteReqSmrtMtr;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreate.SmrtMtrUtilsConncnStsChgReqERPCrteReqUtilsConncnStsChgReq;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreate.UUID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreate.UtilitiesAdvancedMeteringSystemID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreate.UtilitiesConnectionStatusChangeRequestCategoryCode;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreate.UtilitiesConnectionStatusChangeRequestID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuschangerequestcreate.UtilitiesDeviceID;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class StatusChangeRequestCreateMessage extends AbstractSapMessage {

    private final static String ID_XML_NAME = "UtilitiesConnectionStatusChangeRequest.ID";
    private final static String CATEGORY_CODE_XML_NAME = "CategoryCode";
    private final static String PLANNED_PROCESS_XML_NAME = "PlannedProcessingDateTime";
    private final static String CONNECTION_STATUS_XML_NAME = "DeviceConnectionStatus";

    private Instant plannedProcessingDateTime;
    private Map<String, String> deviceConnectionStatus;
    private String id;
    private String requestId;
    private String uuid;
    private String categoryCode;
    private String utilitiesServiceDisconnectionReasonCode;
    private boolean bulk;

    private StatusChangeRequestCreateMessage() {
    }

    public String getId() {
        return id;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getUuid() {
        return uuid;
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

    public boolean isBulk() {
        return bulk;
    }

    public static Builder builder() {
        return new StatusChangeRequestCreateMessage().new Builder();
    }

    public class Builder {

        private Builder() {
        }

        public Builder from(String id, String requestId, String uuid, String categoryCode, String utilitiesServiceDisconnectionReasonCode, Instant plannedProcessingDateTime, Map<String, String> deviceConnectionStatus, boolean bulk) {
            StatusChangeRequestCreateMessage.this.id = id;
            StatusChangeRequestCreateMessage.this.requestId = requestId;
            StatusChangeRequestCreateMessage.this.uuid = uuid;
            StatusChangeRequestCreateMessage.this.categoryCode = categoryCode;
            StatusChangeRequestCreateMessage.this.utilitiesServiceDisconnectionReasonCode = utilitiesServiceDisconnectionReasonCode;
            StatusChangeRequestCreateMessage.this.plannedProcessingDateTime = plannedProcessingDateTime;
            StatusChangeRequestCreateMessage.this.deviceConnectionStatus = deviceConnectionStatus;
            StatusChangeRequestCreateMessage.this.bulk = bulk;
            return this;
        }

        public Builder from(SmrtMtrUtilsConncnStsChgReqERPCrteReqMsg requestMessage) {
            Optional.ofNullable(requestMessage.getMessageHeader()).ifPresent(messageHeader -> {
                setRequestId(getRequestId(messageHeader));
                setUuid(getUuid(messageHeader));
            });
            Optional.ofNullable(requestMessage.getUtilitiesConnectionStatusChangeRequest())
                    .ifPresent(statusChangeRequest -> {
                        setId(getId(statusChangeRequest));
                        setCategoryCode(getCategoryCode(statusChangeRequest));
                        setUtilitiesServiceDisconnectionReasonCode(getUtilitiesServiceDisconnectionReasonCode(statusChangeRequest));
                        setPlannedProcessingDateTime(statusChangeRequest.getPlannedProcessingDateTime());
                        setDeviceConnectionStatus(getDeviceConnectionStatus(statusChangeRequest));
                    });
            StatusChangeRequestCreateMessage.this.bulk = false;
            return this;
        }

        public Builder setId(String id) {
            StatusChangeRequestCreateMessage.this.id = id;
            return this;
        }

        public Builder setRequestId(String requestId) {
            StatusChangeRequestCreateMessage.this.requestId = requestId;
            return this;
        }

        public Builder setUuid(String uuid) {
            StatusChangeRequestCreateMessage.this.uuid = uuid;
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

        public StatusChangeRequestCreateMessage build(Thesaurus thesaurus) {
            if (requestId == null && uuid == null) {
                addAtLeastOneMissingField(thesaurus, REQUEST_ID_XML_NAME, UUID_XML_NAME);
            }
            if (id == null) {
                addMissingField(ID_XML_NAME);
            }
            if (categoryCode == null) {
                addMissingField(CATEGORY_CODE_XML_NAME);
            }
            if (plannedProcessingDateTime == null) {
                addMissingField(PLANNED_PROCESS_XML_NAME);
            }
            if (deviceConnectionStatus.isEmpty()) {
                addMissingField(CONNECTION_STATUS_XML_NAME);
            }
            return StatusChangeRequestCreateMessage.this;
        }

        private String getId(SmrtMtrUtilsConncnStsChgReqERPCrteReqUtilsConncnStsChgReq changeRequest) {
            return Optional.ofNullable(changeRequest.getID())
                    .map(UtilitiesConnectionStatusChangeRequestID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getRequestId(BusinessDocumentMessageHeader header) {
            return Optional.ofNullable(header.getID())
                    .map(BusinessDocumentMessageID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getUuid(BusinessDocumentMessageHeader header) {
            return Optional.ofNullable(header.getUUID())
                    .map(UUID::getValue)
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