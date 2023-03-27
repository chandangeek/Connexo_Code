/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.deviceinitialization.location;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.sap.soap.webservices.impl.AbstractSapMessage;
import com.energyict.mdc.sap.soap.webservices.impl.TranslationKeys;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class LocationMessage extends AbstractSapMessage {
    private static final String LOCATION_ID_XML_NAME = "InstallationPointID";

    private final Thesaurus thesaurus;

    private String deviceId;
    private String locationId;
    private String installationNumber;
    private String pointOfDelivery;
    private String divisionCategoryCode;
    private String locationIdInformation;
    private String modificationInformation;

    private LocationMessage(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
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

    static LocationMessage.Builder builder(Thesaurus thesaurus) {
        return new LocationMessage(thesaurus).new Builder();
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
            return Optional.ofNullable(utilitiesDevice.getLocation())
                    .flatMap(location -> location.stream().findFirst())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.UtilsDvceERPSmrtMtrLocNotifLoc::getInstallationNumber)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getPod(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.UtilsDvceERPSmrtMtrLocNotifUtilsDvce utilitiesDevice) {
            return Optional.ofNullable(utilitiesDevice.getLocation())
                    .flatMap(location -> location.stream().findFirst())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.UtilsDvceERPSmrtMtrLocNotifLoc::getPointOfDelivery)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getDivisionCategoryCode(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.UtilsDvceERPSmrtMtrLocNotifUtilsDvce utilitiesDevice) {
            return Optional.ofNullable(utilitiesDevice.getLocation())
                    .flatMap(location -> location.stream().findFirst())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.UtilsDvceERPSmrtMtrLocNotifLoc::getDivisionCategoryCode)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getLocationIdInformation(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.UtilsDvceERPSmrtMtrLocNotifUtilsDvce utilitiesDevice) {
            return Optional.ofNullable(utilitiesDevice.getLocation())
                    .flatMap(location -> location.stream().findFirst())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.UtilsDvceERPSmrtMtrLocNotifLoc::getInstallationPointAddressInformation)
                    .map(information -> {
                        Map<TranslationKey, String> map = new LinkedHashMap<>();
                        map.put(TranslationKeys.REGION_CODE, information.getRegionCode().getValue());
                        map.put(TranslationKeys.COUNTRY_CODE, information.getCountryCode());
                        map.put(TranslationKeys.CITY_NAME, information.getCityName());
                        map.put(TranslationKeys.STREET_POSTAL_CODE, information.getStreetPostalCode());
                        map.put(TranslationKeys.STREET_NAME, information.getStreetName());
                        map.put(TranslationKeys.HOUSE_ID, information.getHouseID());
                        return map;
                    })
                    .map(this::stringifyAttributes)
                    .orElse(null);
        }

        private String getModificationInformation(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.UtilsDvceERPSmrtMtrLocNotifUtilsDvce utilitiesDevice) {
            return Optional.ofNullable(utilitiesDevice.getLocation())
                    .flatMap(location -> location.stream().findFirst())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationnotification.UtilsDvceERPSmrtMtrLocNotifLoc::getModificationInformation)
                    .map(information -> {
                        Map<TranslationKey, Object> map = new LinkedHashMap<>();
                        map.put(TranslationKeys.INSTALLATION_DATE, information.getInstallationDate());
                        map.put(TranslationKeys.INSTALLATION_TIME, information.getInstallationTime());
                        map.put(TranslationKeys.REMOVE_DATE, information.getRemoveDate());
                        map.put(TranslationKeys.REMOVE_TIME, information.getRemoveTime());
                        return map;
                    })
                    .map(this::stringifyAttributes)
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
            return Optional.ofNullable(utilitiesDevice.getLocation())
                    .flatMap(location -> location.stream().findFirst())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilsDvceERPSmrtMtrLocNotifLoc::getInstallationNumber)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getPod(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilsDvceERPSmrtMtrLocNotifUtilsDvce utilitiesDevice) {
            return Optional.ofNullable(utilitiesDevice.getLocation())
                    .flatMap(location -> location.stream().findFirst())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilsDvceERPSmrtMtrLocNotifLoc::getPointOfDelivery)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getDivisionCategoryCode(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilsDvceERPSmrtMtrLocNotifUtilsDvce utilitiesDevice) {
            return Optional.ofNullable(utilitiesDevice.getLocation())
                    .flatMap(location -> location.stream().findFirst())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilsDvceERPSmrtMtrLocNotifLoc::getDivisionCategoryCode)
                    .filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace())
                    .orElse(null);
        }

        private String getLocationIdInformation(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilsDvceERPSmrtMtrLocNotifUtilsDvce utilitiesDevice) {
            return Optional.ofNullable(utilitiesDevice.getLocation())
                    .flatMap(location -> location.stream().findFirst())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilsDvceERPSmrtMtrLocNotifLoc::getInstallationPointAddressInformation)
                    .map(information -> {
                        Map<TranslationKey, String> map = new LinkedHashMap<>();
                        map.put(TranslationKeys.REGION_CODE, information.getRegionCode().getValue());
                        map.put(TranslationKeys.COUNTRY_CODE, information.getCountryCode());
                        map.put(TranslationKeys.CITY_NAME, information.getCityName());
                        map.put(TranslationKeys.STREET_POSTAL_CODE, information.getStreetPostalCode());
                        map.put(TranslationKeys.STREET_NAME, information.getStreetName());
                        map.put(TranslationKeys.HOUSE_ID, information.getHouseID());
                        return map;
                    })
                    .map(this::stringifyAttributes)
                    .orElse(null);
        }

        private String getModificationInformation(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilsDvceERPSmrtMtrLocNotifUtilsDvce utilitiesDevice) {
            return Optional.ofNullable(utilitiesDevice.getLocation())
                    .flatMap(location -> location.stream().findFirst())
                    .map(com.energyict.mdc.sap.soap.wsdl.webservices.utilitiesdevicelocationbulknotification.UtilsDvceERPSmrtMtrLocNotifLoc::getModificationInformation)
                    .map(information -> {
                        Map<TranslationKey, Object> map = new LinkedHashMap<>();
                        map.put(TranslationKeys.INSTALLATION_DATE, information.getInstallationDate());
                        map.put(TranslationKeys.INSTALLATION_TIME, information.getInstallationTime());
                        map.put(TranslationKeys.REMOVE_DATE, information.getRemoveDate());
                        map.put(TranslationKeys.REMOVE_TIME, information.getRemoveTime());
                        return map;
                    })
                    .map(this::stringifyAttributes)
                    .orElse(null);
        }

        private String stringifyAttributes(Map<? extends TranslationKey, ?> attributes) {
            return attributes.entrySet().stream()
                    .map(attribute -> thesaurus.getFormat(attribute.getKey()).format() + " = " + attribute.getValue())
                    .collect(Collectors.joining(", "));
        }
    }
}
