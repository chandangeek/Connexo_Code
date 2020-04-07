/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.pod;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.sap.soap.webservices.impl.AbstractSapMessage;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodbulknotification.SmrtMtrUtilsMsmtTskERPPtDelivBulkAssgndNotifMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodnotification.SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifMsg;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PodNotificationMessage extends AbstractSapMessage {
    private String requestId;
    private String uuid;
    private boolean bulk;
    private List<PodMessage> podMessages = new ArrayList<>();

    private Thesaurus thesaurus;

    private PodNotificationMessage(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getUuid() {
        return uuid;
    }

    public List<PodMessage> getPodMessages() {
        return podMessages;
    }

    public boolean isBulk() {
        return bulk;
    }

    static PodNotificationMessage.Builder builder(Thesaurus thesaurus) {
        return new PodNotificationMessage(thesaurus).new Builder();
    }

    public class Builder {

        private Builder() {
        }

        public PodNotificationMessage.Builder from(SmrtMtrUtilsMsmtTskERPPtDelivAssgndNotifMsg podNotifMsg) {
            bulk = false;
            Optional.ofNullable(podNotifMsg.getMessageHeader())
                    .ifPresent(messageHeader -> {
                        setRequestID(getRequestId(messageHeader));
                        setUuid(getUuid(messageHeader));
                    });

            podMessages.add(PodMessage
                    .builder()
                    .from(podNotifMsg)
                    .build());
            return this;
        }

        public PodNotificationMessage.Builder from(SmrtMtrUtilsMsmtTskERPPtDelivBulkAssgndNotifMsg podNotifMsg) {
            bulk = true;
            Optional.ofNullable(podNotifMsg.getMessageHeader())
                    .ifPresent(messageHeader -> {
                        setRequestID(getRequestId(messageHeader));
                        setUuid(getUuid(messageHeader));
                    });

            podNotifMsg.getSmartMeterUtilitiesMeasurementTaskERPPointOfDeliveryAssignedNotificationMessage()
                    .forEach(message -> podMessages.add(PodMessage
                            .builder()
                            .from(message)
                            .build())
                    );
            return this;
        }

        public PodNotificationMessage build() {
            if (requestId == null && uuid == null) {
                addAtLeastOneMissingField(thesaurus, REQUEST_ID_XML_NAME, UUID_XML_NAME);
            }
            for (PodMessage message : podMessages) {
                addMissingFields(message.getMissingFieldsSet());
            }
            return PodNotificationMessage.this;
        }

        private void setRequestID(String requestId) {
            PodNotificationMessage.this.requestId = requestId;
        }

        private void setUuid(String uuid) {
            PodNotificationMessage.this.uuid = uuid;
        }

        private String getRequestId(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodnotification.BusinessDocumentMessageHeader header) {
            return Optional.ofNullable(header.getID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodnotification.BusinessDocumentMessageID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getUuid(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodnotification.BusinessDocumentMessageHeader header) {
            return Optional.ofNullable(header.getUUID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodnotification.UUID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getRequestId(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodbulknotification.BusinessDocumentMessageHeader header) {
            return Optional.ofNullable(header.getID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodbulknotification.BusinessDocumentMessageID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getUuid(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodbulknotification.BusinessDocumentMessageHeader header) {
            return Optional.ofNullable(header.getUUID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.smartmeterutilitiespodbulknotification.UUID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }
    }
}
