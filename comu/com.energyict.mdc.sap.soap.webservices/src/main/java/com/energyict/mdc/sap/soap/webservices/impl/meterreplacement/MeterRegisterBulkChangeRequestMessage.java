/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.meterreplacement;

import com.elster.jupiter.util.Checks;

import com.energyict.mdc.sap.soap.webservices.impl.AbstractSapMessage;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkrequest.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkrequest.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkrequest.UUID;
import com.energyict.mdc.sap.soap.wsdl.webservices.meterreplacementbulkrequest.UtilsDvceERPSmrtMtrRegBulkChgReqMsg;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MeterRegisterBulkChangeRequestMessage extends AbstractSapMessage {
    private final Integer meterReplacementAddInterval;

    private String requestId;
    private String uuid;
    private List<MeterRegisterChangeMessage> meterRegisterChangeMessages = new ArrayList<>();

    private MeterRegisterBulkChangeRequestMessage(Integer meterReplacementAddInterval) {
        this.meterReplacementAddInterval = meterReplacementAddInterval;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getUuid() {
        return uuid;
    }

    public List<MeterRegisterChangeMessage> getMeterRegisterChangeMessages() {
        return meterRegisterChangeMessages;
    }

    static MeterRegisterBulkChangeRequestMessage.Builder builder(Integer meterReplacementAddInterval) {
        return new MeterRegisterBulkChangeRequestMessage(meterReplacementAddInterval).new Builder();
    }

    public class Builder {

        private Builder() {
        }

        public MeterRegisterBulkChangeRequestMessage.Builder from(UtilsDvceERPSmrtMtrRegBulkChgReqMsg requestMessage) {
            Optional.ofNullable(requestMessage.getMessageHeader())
                    .ifPresent(messageHeader -> {
                        setRequestId(getRequestId(messageHeader));
                        setUuid(getUuid(messageHeader));
                    });

            requestMessage.getUtilitiesDeviceERPSmartMeterRegisterChangeRequestMessage()
                    .forEach(message ->
                            meterRegisterChangeMessages.add(MeterRegisterChangeBulkMessageBuilder
                                    .builder(meterReplacementAddInterval)
                                    .from(message)
                                    .build()));
            return this;
        }

        public MeterRegisterBulkChangeRequestMessage build() {
            if (requestId == null && uuid == null) {
                addAtLeastOneNotValid(REQUEST_ID_XML_NAME, UUID_XML_NAME);
            }
            return MeterRegisterBulkChangeRequestMessage.this;
        }

        private void setRequestId(String requestId) {
            MeterRegisterBulkChangeRequestMessage.this.requestId = requestId;
        }

        private void setUuid(String uuid) {
            MeterRegisterBulkChangeRequestMessage.this.uuid = uuid;
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
    }
}
