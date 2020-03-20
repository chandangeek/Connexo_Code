/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.location;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.sap.soap.webservices.impl.AbstractSapMessage;

import java.util.Optional;

public class LocationMessage extends AbstractSapMessage {
    private static final String LOCATION_ID_XML_NAME = "InstallationPointID";

    private String deviceId;
    private String locationId;

    private LocationMessage() {
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getLocationId() {
        return locationId;
    }

    static LocationMessage.Builder builder() {
        return new LocationMessage().new Builder();
    }

    public class Builder {

        private Builder() {
        }

        public LocationMessage.Builder from(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.UtilsDvceERPSmrtMtrLocNotifMsg locNotifMsg) {
            Optional.ofNullable(locNotifMsg.getUtilitiesDevice())
                    .ifPresent(utilitiesDevice -> {
                        setDeviceId(getDeviceId(utilitiesDevice));
                        setLocationId(getLocationId(utilitiesDevice));
                    });
            return this;
        }

        public LocationMessage.Builder from(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilsDvceERPSmrtMtrLocNotifMsg locNotifMsg) {
            Optional.ofNullable(locNotifMsg.getUtilitiesDevice())
                    .ifPresent(utilitiesDevice -> {
                        setDeviceId(getDeviceId(utilitiesDevice));
                        setLocationId(getLocationId(utilitiesDevice));
                    });
            return this;
        }

        public LocationMessage build() {
            if (deviceId == null) {
                addMissingField(UTILITIES_DEVICE_ID_XML_NAME);
            }
            if (locationId == null) {
                addMissingField(LOCATION_ID_XML_NAME);
            }
            return LocationMessage.this;
        }

        private void setDeviceId(String deviceId) {
            LocationMessage.this.deviceId = deviceId;
        }

        private void setLocationId(String locationId) {
            LocationMessage.this.locationId = locationId;
        }

        private String getDeviceId(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.UtilsDvceERPSmrtMtrLocNotifUtilsDvce utilitiesDevice) {
            return Optional.ofNullable(utilitiesDevice.getID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.UtilitiesDeviceID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getLocationId(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.UtilsDvceERPSmrtMtrLocNotifUtilsDvce utilitiesDevice) {
            return Optional.ofNullable(utilitiesDevice.getLocation())
                    .flatMap(location -> location.stream().findFirst())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.UtilsDvceERPSmrtMtrLocNotifLoc::getInstallationPointID)
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.InstallationPointID::getValue)
                    .orElse(null);
        }

        private String getDeviceId(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilsDvceERPSmrtMtrLocNotifUtilsDvce utilitiesDevice) {
            return Optional.ofNullable(utilitiesDevice.getID())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilitiesDeviceID::getValue)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getLocationId(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilsDvceERPSmrtMtrLocNotifUtilsDvce utilitiesDevice) {
            return Optional.ofNullable(utilitiesDevice.getLocation())
                    .flatMap(location -> location.stream().findFirst())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilsDvceERPSmrtMtrLocNotifLoc::getInstallationPointID)
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.InstallationPointID::getValue)
                    .orElse(null);
        }
    }
}
