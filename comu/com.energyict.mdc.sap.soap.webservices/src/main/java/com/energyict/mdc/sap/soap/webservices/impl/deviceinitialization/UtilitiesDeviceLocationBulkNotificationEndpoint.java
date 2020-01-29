/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.SapAttributeNames;
import com.energyict.mdc.sap.soap.webservices.impl.AbstractSapMessage;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.InstallationPointID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UUID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilitiesDeviceERPSmartMeterLocationBulkNotificationCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilsDvceERPSmrtMtrLocBulkNotifMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilsDvceERPSmrtMtrLocNotifLoc;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilsDvceERPSmrtMtrLocNotifMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilsDvceERPSmrtMtrLocNotifUtilsDvce;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UtilitiesDeviceLocationBulkNotificationEndpoint extends AbstractInboundEndPoint implements UtilitiesDeviceERPSmartMeterLocationBulkNotificationCIn, ApplicationSpecific {

    private final SAPCustomPropertySets sapCustomPropertySets;
    private final Thesaurus thesaurus;

    @Inject
    UtilitiesDeviceLocationBulkNotificationEndpoint(SAPCustomPropertySets sapCustomPropertySets, Thesaurus thesaurus) {
        this.sapCustomPropertySets = sapCustomPropertySets;
        this.thesaurus = thesaurus;
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }

    @Override
    public void utilitiesDeviceERPSmartMeterLocationBulkNotificationCIn(UtilsDvceERPSmrtMtrLocBulkNotifMsg request) {
        runInTransactionWithOccurrence(() -> {
            Optional.ofNullable(request)
                    .ifPresent(requestMessage -> handleMessage(requestMessage));
            return null;
        });
    }

    private void handleMessage(UtilsDvceERPSmrtMtrLocBulkNotifMsg msg) {
        LocationBulkMessage bulkMsg = new LocationBulkMessage(msg);
        SetMultimap<String, String> values = HashMultimap.create();
        bulkMsg.locationMessages.forEach(message -> values.put(SapAttributeNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), message.deviceId));
        saveRelatedAttributes(values);

        if (bulkMsg.isValid()) {
            bulkMsg.locationMessages.forEach(message -> {
                if (message.isValid()) {
                    Optional<Device> device = sapCustomPropertySets.getDevice(message.deviceId);
                    if (device.isPresent()) {
                        try {
                            sapCustomPropertySets.setLocation(device.get(), message.locationId);
                        } catch (LocalizedException ex) {
                            log(LogLevel.WARNING, thesaurus.getFormat(ex.getMessageSeed()).format(ex.getMessageArgs()));
                        }
                    } else {
                        log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.NO_DEVICE_FOUND_BY_SAP_ID).format(message.deviceId));
                    }
                } else {
                    log(LogLevel.WARNING, MessageSeeds.INVALID_MESSAGE_FORMAT.getDefaultFormat(message.getNotValidFields()));
                }
            });
        } else {
            log(LogLevel.WARNING, MessageSeeds.INVALID_MESSAGE_FORMAT.getDefaultFormat(bulkMsg.getNotValidFields()));
        }
    }

    private class LocationBulkMessage extends AbstractSapMessage {
        private String requestId;
        private String uuid;
        private List<LocationMessage> locationMessages = new ArrayList<>();

        private LocationBulkMessage(UtilsDvceERPSmrtMtrLocBulkNotifMsg msg) {
            requestId = getRequestId(msg);
            uuid = getUuid(msg);
            if (requestId == null && uuid == null) {
                addAtLeastOneNotValid(REQUEST_ID_XML_NAME, UUID_XML_NAME);
            }
            msg.getUtilitiesDeviceERPSmartMeterLocationNotificationMessage()
                    .forEach(message -> {
                        LocationMessage locationMsg = new LocationMessage(message);
                        locationMessages.add(locationMsg);
                    });
        }

        private String getRequestId(UtilsDvceERPSmrtMtrLocBulkNotifMsg msg) {
            return Optional.ofNullable(msg.getMessageHeader())
                    .map(BusinessDocumentMessageHeader::getID)
                    .map(BusinessDocumentMessageID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getUuid(UtilsDvceERPSmrtMtrLocBulkNotifMsg msg) {
            return Optional.ofNullable(msg.getMessageHeader())
                    .map(BusinessDocumentMessageHeader::getUUID)
                    .map(UUID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }
    }

    private class LocationMessage extends AbstractSapMessage {

        private static final String UTILITIES_DEVICE_ID_XML_NAME = "UtilitiesDevice.UtilitiesDeviceID";
        private static final String LOCATION_ID_XML_NAME = "UtilitiesDevice.Location.InstallationPointID";

        private String deviceId;
        private String locationId;

        private LocationMessage(UtilsDvceERPSmrtMtrLocNotifMsg msg) {
            deviceId = getDeviceId(msg);
            locationId = getLocationId(msg);
            if (deviceId == null) {
                addNotValidField(UTILITIES_DEVICE_ID_XML_NAME);
            }
            if (locationId == null) {
                addNotValidField(LOCATION_ID_XML_NAME);
            }
        }

        private String getDeviceId(UtilsDvceERPSmrtMtrLocNotifMsg msg) {
            return Optional.ofNullable(msg.getUtilitiesDevice())
                    .map(UtilsDvceERPSmrtMtrLocNotifUtilsDvce::getID)
                    .map(UtilitiesDeviceID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getLocationId(UtilsDvceERPSmrtMtrLocNotifMsg msg) {
            return Optional.ofNullable(msg.getUtilitiesDevice())
                    .map(UtilsDvceERPSmrtMtrLocNotifUtilsDvce::getLocation)
                    .flatMap(location -> location.stream().findFirst())
                    .map(UtilsDvceERPSmrtMtrLocNotifLoc::getInstallationPointID)
                    .map(InstallationPointID::getValue)
                    .orElse(null);
        }
    }
}
