/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection.cancellation;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationrequest.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationrequest.UUID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationrequest.UtilitiesConnectionStatusChangeRequestID;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationrequest.SmrtMtrUtilsConncnStsChgReqERPCanclnReqMsg;

import java.util.Optional;

public class StatusChangeRequestCancellationRequestMessage {
    private String id;
    private String requestId;
    private String uuid;
    private String categoryCode;

    private StatusChangeRequestCancellationRequestMessage() {
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

    static StatusChangeRequestCancellationRequestMessage.Builder builder() {
        return new StatusChangeRequestCancellationRequestMessage().new Builder();
    }

    public boolean isValid() {
        return (requestId != null || uuid != null) && id != null && categoryCode != null;
    }

    public class Builder {

        private Builder() {
        }

        public StatusChangeRequestCancellationRequestMessage.Builder from(SmrtMtrUtilsConncnStsChgReqERPCanclnReqMsg requestMessage) {
            setId(getId(requestMessage));
            setRequestId(getRequestId(requestMessage));
            setUuid(getUuid(requestMessage));
            setCategoryCode(getCategoryCode(requestMessage));
            return this;
        }

        public StatusChangeRequestCancellationRequestMessage build() {
            return StatusChangeRequestCancellationRequestMessage.this;
        }

        private void setId(String id) {
            StatusChangeRequestCancellationRequestMessage.this.id = id;
        }

        private void setRequestId(String requestId) {
            StatusChangeRequestCancellationRequestMessage.this.requestId = requestId;
        }

        private void setUuid(String uuid) {
            StatusChangeRequestCancellationRequestMessage.this.uuid = uuid;
        }

        private void setCategoryCode(String categoryCode) {
            StatusChangeRequestCancellationRequestMessage.this.categoryCode = categoryCode;
        }

        private String getId(SmrtMtrUtilsConncnStsChgReqERPCanclnReqMsg msg) {
            return Optional.ofNullable(msg.getUtilitiesConnectionStatusChangeRequest())
                    .map(m -> m.getID())
                    .map(UtilitiesConnectionStatusChangeRequestID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getRequestId(SmrtMtrUtilsConncnStsChgReqERPCanclnReqMsg msg) {
            return Optional.ofNullable(msg.getMessageHeader())
                    .map(m -> m.getID())
                    .map(BusinessDocumentMessageID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getUuid(SmrtMtrUtilsConncnStsChgReqERPCanclnReqMsg msg) {
            return Optional.ofNullable(msg.getMessageHeader())
                    .map(m -> m.getUUID())
                    .map(UUID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getCategoryCode(SmrtMtrUtilsConncnStsChgReqERPCanclnReqMsg msg) {
            return Optional.ofNullable(msg.getUtilitiesConnectionStatusChangeRequest())
                    .map(m -> m.getCategoryCode())
                    .map(c -> c.getValue())
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

    }
}
