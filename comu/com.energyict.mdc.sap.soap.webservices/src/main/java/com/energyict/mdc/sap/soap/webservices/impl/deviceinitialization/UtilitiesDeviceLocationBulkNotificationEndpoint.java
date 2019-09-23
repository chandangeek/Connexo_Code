/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.AbstractInboundEndPoint;
import com.elster.jupiter.soap.whiteboard.cxf.ApplicationSpecific;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceRequestAttributesNames;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.InstallationPointID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilitiesDeviceERPSmartMeterLocationBulkNotificationCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilsDvceERPSmrtMtrLocBulkNotifMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilsDvceERPSmrtMtrLocNotifLoc;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilsDvceERPSmrtMtrLocNotifMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilsDvceERPSmrtMtrLocNotifUtilsDvce;

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
        if (bulkMsg.isValid()) {
            bulkMsg.locationMessages.forEach(message -> {
                if (message.isValid()) {
                    createRelatedObject(WebServiceRequestAttributesNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), message.deviceId);
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
                    log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.INVALID_MESSAGE_FORMAT).format());
                }
            });
        } else {
            log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.INVALID_MESSAGE_FORMAT).format());
        }
    }

    private class LocationBulkMessage {
        private String requestId;
        private List<LocationMessage> locationMessages = new ArrayList<>();

        private LocationBulkMessage(UtilsDvceERPSmrtMtrLocBulkNotifMsg msg) {
            requestId = getRequestId(msg);
            msg.getUtilitiesDeviceERPSmartMeterLocationNotificationMessage()
                    .forEach(message -> {
                        LocationMessage locationMsg = new LocationMessage(message);
                        locationMessages.add(locationMsg);
                    });
        }

        private boolean isValid() {
            return requestId != null;
        }

        private String getRequestId(UtilsDvceERPSmrtMtrLocBulkNotifMsg msg) {
            return Optional.ofNullable(msg.getMessageHeader())
                    .map(BusinessDocumentMessageHeader::getID)
                    .map(BusinessDocumentMessageID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }
    }

    private class LocationMessage {
        private String deviceId;
        private String locationId;

        private LocationMessage(UtilsDvceERPSmrtMtrLocNotifMsg msg) {
            deviceId = getDeviceId(msg);
            locationId = getLocationId(msg);
        }

        private boolean isValid() {
            return deviceId != null && locationId != null;
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
