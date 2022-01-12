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
    private String installationNumber;
    private String pointOfDelivery;
    private String divisionCategoryCode;
    private String locationIdInformation;
    private String modificationInformation;

    private LocationMessage() {
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getLocationId() {
        return locationId;
    }

    public String getInstallationNumber() {
        return installationNumber;
    }

    public String getPod() {
        return pointOfDelivery;
    }

    public String getDivisionCategoryCode() {
        return divisionCategoryCode;
    }

    public String getLocationIdInformation() {
        return locationIdInformation;
    }

    public String getModificationInformation() {
        return modificationInformation;
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
                        setInstallationNumber(getInstallationNumber(utilitiesDevice));
                        setPod(getPod(utilitiesDevice));
                        setDivisionCategoryCode(getDivisionCategoryCode(utilitiesDevice));
                        setLocationIdInformation(getLocationIdInformation(utilitiesDevice));
                        setModificationInformation(getModificationInformation(utilitiesDevice));
                    });
            return this;
        }

        public LocationMessage.Builder from(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilsDvceERPSmrtMtrLocNotifMsg locNotifMsg) {
            Optional.ofNullable(locNotifMsg.getUtilitiesDevice())
                    .ifPresent(utilitiesDevice -> {
                        setDeviceId(getDeviceId(utilitiesDevice));
                        setLocationId(getLocationId(utilitiesDevice));
                        setInstallationNumber(getInstallationNumber(utilitiesDevice));
                        setPod(getPod(utilitiesDevice));
                        setDivisionCategoryCode(getDivisionCategoryCode(utilitiesDevice));
                        setLocationIdInformation(getLocationIdInformation(utilitiesDevice));
                        setModificationInformation(getModificationInformation(utilitiesDevice));
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

        private void setInstallationNumber(String installationNumber) {
            LocationMessage.this.installationNumber = installationNumber;
        }

        private void setPod(String pointOfDelivery) {
            LocationMessage.this.pointOfDelivery = pointOfDelivery;
        }

        private void setDivisionCategoryCode(String divisionCategoryCode) {
            LocationMessage.this.divisionCategoryCode = divisionCategoryCode;
        }

        private void setLocationIdInformation(String locationIdInformation) {
            LocationMessage.this.locationIdInformation = locationIdInformation;
        }

        private void setModificationInformation(String modificationInformation) {
            LocationMessage.this.modificationInformation = modificationInformation;
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

        private String getInstallationNumber(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.UtilsDvceERPSmrtMtrLocNotifUtilsDvce utilitiesDevice) {
//            return Optional.ofNullable(utilitiesDevice.getLocation())
//                    .flatMap(location -> location.stream().findFirst())
//                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.UtilsDvceERPSmrtMtrLocNotifLoc::getInstallationNumber)
//                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.InstallationPointID::getValue)
//                    .orElse(null);
            //TODO use values from wsdl
            return "InstallationNumber" + Math.random();
        }

        private String getPod(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.UtilsDvceERPSmrtMtrLocNotifUtilsDvce utilitiesDevice) {
//            return Optional.ofNullable(utilitiesDevice.getLocation())
//                    .flatMap(location -> location.stream().findFirst())
//                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.UtilsDvceERPSmrtMtrLocNotifLoc::getInstallationNumber)
//                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.InstallationPointID::getValue)
//                    .orElse(null);
            //TODO use values from wsdl
            return "PointOfDelivery" + Math.random();
        }

        private String getDivisionCategoryCode(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.UtilsDvceERPSmrtMtrLocNotifUtilsDvce utilitiesDevice) {
//            return Optional.ofNullable(utilitiesDevice.getLocation())
//                    .flatMap(location -> location.stream().findFirst())
//                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.UtilsDvceERPSmrtMtrLocNotifLoc::getInstallationNumber)
//                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.InstallationPointID::getValue)
//                    .orElse(null);
            //TODO use values from wsdl
            return "DivisionCategoryCode" + Math.random();
        }

        private String getLocationIdInformation(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.UtilsDvceERPSmrtMtrLocNotifUtilsDvce utilitiesDevice) {
            return Optional.ofNullable(utilitiesDevice.getLocation())
                    .flatMap(location -> location.stream().findFirst())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.UtilsDvceERPSmrtMtrLocNotifLoc::getInstallationPointAddressInformation)
                    .map(information -> String.format("Region code =  %s\n,Country code =  %s\n,City name = %s\n,Street postal code = %s\n,Street name = %s\n,Houde id = %s\n", information.getRegionCode()
                            .getValue(), information.getCountryCode(), information.getCityName(), information.getStreetPostalCode(), information.getStreetName(), information.getHouseID()))
                    .orElse(null);
        }

        private String getModificationInformation(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.UtilsDvceERPSmrtMtrLocNotifUtilsDvce utilitiesDevice) {
            return Optional.ofNullable(utilitiesDevice.getLocation())
                    .flatMap(location -> location.stream().findFirst())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.UtilsDvceERPSmrtMtrLocNotifLoc::getModificationInformation)
                    .map(information -> String.format("Installation date =  %s ,Installation time =  %s ,Remove date = %s ,Remove time = %s ", information.getInstallationDate(), information.getInstallationTime(), information
                            .getRemoveDate(), information.getRemoveTime()))
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

        private String getInstallationNumber(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilsDvceERPSmrtMtrLocNotifUtilsDvce utilitiesDevice) {
//            return Optional.ofNullable(utilitiesDevice.getLocation())
//                    .flatMap(location -> location.stream().findFirst())
//                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilsDvceERPSmrtMtrLocNotifLoc::getInstallationPointID)
//                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.InstallationPointID::getValue)
//                    .orElse(null);
            //TODO use values from wsdl
            return "InstallationNumber" + Math.random();
        }

        private String getPod(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilsDvceERPSmrtMtrLocNotifUtilsDvce utilitiesDevice) {
//            return Optional.ofNullable(utilitiesDevice.getLocation())
//                    .flatMap(location -> location.stream().findFirst())
//                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilsDvceERPSmrtMtrLocNotifLoc::getInstallationPointID)
//                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.InstallationPointID::getValue)
//                    .orElse(null);
            //TODO use values from wsdl
            return "PointOfDelivery" + Math.random();
        }

        private String getDivisionCategoryCode(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilsDvceERPSmrtMtrLocNotifUtilsDvce utilitiesDevice) {
//            return Optional.ofNullable(utilitiesDevice.getLocation())
//                    .flatMap(location -> location.stream().findFirst())
//                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.UtilsDvceERPSmrtMtrLocNotifLoc::getInstallationNumber)
//                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.InstallationPointID::getValue)
//                    .orElse(null);
            //TODO use values from wsdl
            return "DivisionCategoryCode" + Math.random();
        }

        private String getLocationIdInformation(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilsDvceERPSmrtMtrLocNotifUtilsDvce utilitiesDevice) {
            return Optional.ofNullable(utilitiesDevice.getLocation())
                    .flatMap(location -> location.stream().findFirst())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilsDvceERPSmrtMtrLocNotifLoc::getInstallationPointAddressInformation)
                    .map(information -> String.format("Region code =  %s\n,Country code =  %s\n,City name = %s\n,Street postal code = %s\n,Street name = %s\n,Houde id = %s\n", information.getRegionCode()
                            .getValue(), information.getCountryCode(), information.getCityName(), information.getStreetPostalCode(), information.getStreetName(), information.getHouseID()))
                    .orElse(null);
        }

        private String getModificationInformation(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilsDvceERPSmrtMtrLocNotifUtilsDvce utilitiesDevice) {
            return Optional.ofNullable(utilitiesDevice.getLocation())
                    .flatMap(location -> location.stream().findFirst())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilsDvceERPSmrtMtrLocNotifLoc::getModificationInformation)
                    .map(information -> String.format("Installation date =  %s ,Installation time =  %s ,Removal date = %s ,Removal time = %s ", information.getInstallationDate(), information.getInstallationTime(), information
                            .getRemoveDate(), information.getRemoveTime()))
                    .orElse(null);
        }

    }
}
