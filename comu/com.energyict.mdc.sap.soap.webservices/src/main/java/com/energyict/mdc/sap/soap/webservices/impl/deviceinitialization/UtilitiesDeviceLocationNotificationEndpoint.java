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
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.BusinessDocumentMessageHeader;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.BusinessDocumentMessageID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.InstallationPointID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.UtilitiesDeviceERPSmartMeterLocationNotificationCIn;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.UtilitiesDeviceID;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.UtilsDvceERPSmrtMtrLocNotifLoc;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.UtilsDvceERPSmrtMtrLocNotifMsg;
import com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.UtilsDvceERPSmrtMtrLocNotifUtilsDvce;

import javax.inject.Inject;
import java.util.Optional;

public class UtilitiesDeviceLocationNotificationEndpoint extends AbstractInboundEndPoint implements UtilitiesDeviceERPSmartMeterLocationNotificationCIn, ApplicationSpecific {

    private final SAPCustomPropertySets sapCustomPropertySets;
    private final Thesaurus thesaurus;

    @Inject
    UtilitiesDeviceLocationNotificationEndpoint(SAPCustomPropertySets sapCustomPropertySets, Thesaurus thesaurus) {
        this.sapCustomPropertySets = sapCustomPropertySets;
        this.thesaurus = thesaurus;
    }

    @Override
    public String getApplication() {
        return ApplicationSpecific.WebServiceApplicationName.MULTISENSE.getName();
    }

    @Override
    public void utilitiesDeviceERPSmartMeterLocationNotificationCIn(UtilsDvceERPSmrtMtrLocNotifMsg request) {
        runInTransactionWithOccurrence(() -> {
            Optional.ofNullable(request)
                    .ifPresent(requestMessage -> handleMessage(requestMessage));
            return null;
        });
    }

    private void handleMessage(UtilsDvceERPSmrtMtrLocNotifMsg msg) {
        LocationMessage locationMsg = new LocationMessage(msg);
        if (locationMsg.isValid()) {
            createRelatedObject( WebServiceRequestAttributesNames.SAP_UTILITIES_DEVICE_ID.getAttributeName(), locationMsg.deviceId);
            Optional<Device> device = sapCustomPropertySets.getDevice(locationMsg.deviceId);
            if (device.isPresent()) {
                try {
                    sapCustomPropertySets.setLocation(device.get(), locationMsg.locationId);
                } catch (LocalizedException ex) {
                    log(LogLevel.WARNING, thesaurus.getFormat(ex.getMessageSeed()).format(ex.getMessageArgs()));
                }
            } else {
                log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.NO_DEVICE_FOUND_BY_SAP_ID).format(locationMsg.deviceId));
            }
        } else {
            log(LogLevel.WARNING, thesaurus.getFormat(MessageSeeds.INVALID_MESSAGE_FORMAT).format());
        }
    }

    private class LocationMessage {
        private String requestId;
        private String deviceId;
        private String locationId;

        private LocationMessage(UtilsDvceERPSmrtMtrLocNotifMsg msg) {
            requestId = getRequestId(msg);
            deviceId = getDeviceId(msg);
            locationId = getLocationId(msg);
        }

        private boolean isValid() {
            return requestId != null && deviceId != null && locationId != null;
        }

        private String getRequestId(UtilsDvceERPSmrtMtrLocNotifMsg msg) {
            return Optional.ofNullable(msg.getMessageHeader())
                    .map(BusinessDocumentMessageHeader::getID)
                    .map(BusinessDocumentMessageID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
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
