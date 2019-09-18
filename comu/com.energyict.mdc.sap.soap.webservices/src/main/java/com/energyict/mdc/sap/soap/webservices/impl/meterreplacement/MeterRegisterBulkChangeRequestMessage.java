/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreplacement;

import com.elster.jupiter.util.Checks;

import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacement.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacement.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacement.UtilsDvceERPSmrtMtrRegBulkChgReqMsg;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MeterRegisterBulkChangeRequestMessage {

    private String requestId;
    private List<MeterRegisterChangeMessage> meterRegisterChangeMessages = new ArrayList<>();

    private MeterRegisterBulkChangeRequestMessage() {
    }

    public String getRequestId() {
        return requestId;
    }

    public List<MeterRegisterChangeMessage> getMeterRegisterChangeMessages() {
        return meterRegisterChangeMessages;
    }

    static MeterRegisterBulkChangeRequestMessage.Builder builder() {
        return new MeterRegisterBulkChangeRequestMessage().new Builder();
    }

    public boolean isValid() {
        return requestId != null;
    }

    public class Builder {

        private Builder() {
        }

        public MeterRegisterBulkChangeRequestMessage.Builder from(UtilsDvceERPSmrtMtrRegBulkChgReqMsg requestMessage) {
            Optional.ofNullable(requestMessage.getMessageHeader())
                    .ifPresent(messageHeader -> {
                        setRequestId(getRequestId(messageHeader));
                    });

            requestMessage.getUtilitiesDeviceERPSmartMeterRegisterChangeRequestMessage()
                    .forEach(message ->
                            meterRegisterChangeMessages.add(MeterRegisterChangeMessage
                                    .builder()
                                    .from(message)
                                    .build()));
            return this;
        }

        public MeterRegisterBulkChangeRequestMessage build() {
            return MeterRegisterBulkChangeRequestMessage.this;
        }

        private void setRequestId(String requestId) {
            MeterRegisterBulkChangeRequestMessage.this.requestId = requestId;
        }

        private String getRequestId(BusinessDocumentMessageHeader header) {
            return Optional.ofNullable(header.getID())
                    .map(BusinessDocumentMessageID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }
    }
}
