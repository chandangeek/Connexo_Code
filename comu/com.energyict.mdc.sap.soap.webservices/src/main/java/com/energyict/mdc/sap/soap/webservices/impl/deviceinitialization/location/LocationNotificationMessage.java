/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.location;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.sap.soap.webservices.impl.AbstractSapMessage;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilsDvceERPSmrtMtrLocBulkNotifMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.UtilsDvceERPSmrtMtrLocNotifMsg;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LocationNotificationMessage extends AbstractSapMessage {
    private String requestId;
    private String uuid;
    private boolean bulk;
    private final List<LocationMessage> locationMessages = new ArrayList<>();

    private final Thesaurus thesaurus;

    private LocationNotificationMessage(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getUuid() {
        return uuid;
    }

    public boolean isBulk() {
        return bulk;
    }

    public List<LocationMessage> getLocationMessages() {
        return locationMessages;
    }

    static LocationNotificationMessage.Builder builder(Thesaurus thesaurus) {
        return new LocationNotificationMessage(thesaurus).new Builder();
    }

    public class Builder {

        private Builder() {
        }

        public LocationNotificationMessage.Builder from(UtilsDvceERPSmrtMtrLocNotifMsg locNotifMsg) {
            bulk = false;
            Optional.ofNullable(locNotifMsg.getMessageHeader())
                    .ifPresent(messageHeader -> {
                        setRequestID(getRequestId(messageHeader));
                        setUuid(getUuid(messageHeader));
                    });

            locationMessages.add(LocationMessage
                    .builder(thesaurus)
                    .from(locNotifMsg)
                    .build());
            return this;
        }

        public LocationNotificationMessage.Builder from(UtilsDvceERPSmrtMtrLocBulkNotifMsg locNotifMsg) {
            bulk = true;
            Optional.ofNullable(locNotifMsg.getMessageHeader())
                    .ifPresent(messageHeader -> {
                        setRequestID(getRequestId(messageHeader));
                        setUuid(getUuid(messageHeader));
                    });

            locNotifMsg.getUtilitiesDeviceERPSmartMeterLocationNotificationMessage()
                    .forEach(message -> locationMessages.add(LocationMessage
                            .builder(thesaurus)
                            .from(message)
                            .build())
                    );
            return this;
        }

        public LocationNotificationMessage build() {
            if (requestId == null && uuid == null) {
                addAtLeastOneMissingField(thesaurus, REQUEST_ID_XML_NAME, UUID_XML_NAME);
            }
            for (LocationMessage message : locationMessages) {
                addMissingFields(message.getMissingFieldsSet());
            }
            return LocationNotificationMessage.this;
        }

        private void setRequestID(String requestId) {
            LocationNotificationMessage.this.requestId = requestId;
        }

        private void setUuid(String uuid) {
            LocationNotificationMessage.this.uuid = uuid;
        }

        private String getRequestId(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.BusinessDocumentMessageHeader header) {
            return Optional.ofNullable(header.getID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.BusinessDocumentMessageID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getUuid(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.BusinessDocumentMessageHeader header) {
            return Optional.ofNullable(header.getUUID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.UUID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getRequestId(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.BusinessDocumentMessageHeader header) {
            return Optional.ofNullable(header.getID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.BusinessDocumentMessageID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getUuid(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.BusinessDocumentMessageHeader header) {
            return Optional.ofNullable(header.getUUID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UUID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }
    }
}
