/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreplacement.meterchange;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.sap.soap.webservices.impl.AbstractSapMessage;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterchangerequest.UtilsDvceERPSmrtMtrChgReqMsg;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UtilitiesDeviceMeterChangeRequestMessage extends AbstractSapMessage {

    private String requestID;
    private String uuid;
    private List<MeterChangeMessage> meterChangeMessages = new ArrayList<>();

    private UtilitiesDeviceMeterChangeRequestMessage() {
    }


    public String getRequestID() {
        return requestID;
    }

    public String getUuid() {
        return uuid;
    }

    public List<MeterChangeMessage> getMeterChangeMessages() {
        return meterChangeMessages;
    }


    static UtilitiesDeviceMeterChangeRequestMessage.Builder builder() {
        return new UtilitiesDeviceMeterChangeRequestMessage().new Builder();
    }

    public class Builder {

        private Builder() {
        }

        public UtilitiesDeviceMeterChangeRequestMessage.Builder from(UtilsDvceERPSmrtMtrChgReqMsg requestMessage) {
            Optional.ofNullable(requestMessage.getMessageHeader())
                    .ifPresent(messageHeader -> {
                        setRequestID(getRequestID(messageHeader));
                        setUuid(getUuid(messageHeader));
                    });

            meterChangeMessages.add(MeterChangeMessage
                    .builder()
                    .from(requestMessage)
                    .build());
            return this;
        }

        public UtilitiesDeviceMeterChangeRequestMessage build() {
            return UtilitiesDeviceMeterChangeRequestMessage.this;
        }

        private void setRequestID(String requestID) {
            UtilitiesDeviceMeterChangeRequestMessage.this.requestID = requestID;
        }

        private void setUuid(String uuid) {
            UtilitiesDeviceMeterChangeRequestMessage.this.uuid = uuid;
        }

        private String getRequestID(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterchangerequest.BusinessDocumentMessageHeader header) {
            return Optional.ofNullable(header.getID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterchangerequest.BusinessDocumentMessageID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }


        private String getUuid(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterchangerequest.BusinessDocumentMessageHeader header) {
            return Optional.ofNullable(header.getUUID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterchangerequest.UUID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }
    }
}
