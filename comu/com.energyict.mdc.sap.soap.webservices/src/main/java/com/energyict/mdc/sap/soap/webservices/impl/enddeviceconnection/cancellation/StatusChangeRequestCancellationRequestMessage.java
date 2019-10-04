/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.enddeviceconnection.cancellation;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationrequest.SmrtMtrUtilsConncnStsChgReqERPCanclnReqMsg;

import java.util.Optional;

public class StatusChangeRequestCancellationRequestMessage {
    private String requestId;
    private String categoryCode;

    private StatusChangeRequestCancellationRequestMessage() {
    }

    public String getRequestId() {
        return requestId;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    static StatusChangeRequestCancellationRequestMessage.Builder builder() {
        return new StatusChangeRequestCancellationRequestMessage().new Builder();
    }

    public boolean isValid() {
        return requestId != null && categoryCode != null;
    }

    public class Builder {

        private Builder() {
        }

        public StatusChangeRequestCancellationRequestMessage.Builder from(SmrtMtrUtilsConncnStsChgReqERPCanclnReqMsg requestMessage) {
            setRequestID(getRequestID(requestMessage));
            setCategoryCode(getCategoryCode(requestMessage));
            return this;
        }

        public StatusChangeRequestCancellationRequestMessage build() {
            return StatusChangeRequestCancellationRequestMessage.this;
        }

        private void setRequestID(String requestID) {
            StatusChangeRequestCancellationRequestMessage.this.requestId = requestID;
        }

        private void setCategoryCode(String categoryCode) {
            StatusChangeRequestCancellationRequestMessage.this.categoryCode = categoryCode;
        }

        private String getRequestID(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationrequest.SmrtMtrUtilsConncnStsChgReqERPCanclnReqMsg msg) {
            return Optional.ofNullable(msg.getUtilitiesConnectionStatusChangeRequest())
                    .map(m -> m.getID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationrequest.UtilitiesConnectionStatusChangeRequestID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getCategoryCode(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterconnectionstatuscancellationrequest.SmrtMtrUtilsConncnStsChgReqERPCanclnReqMsg msg) {
            return Optional.ofNullable(msg.getUtilitiesConnectionStatusChangeRequest())
                    .map(m -> m.getCategoryCode().getValue())
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

    }
}
