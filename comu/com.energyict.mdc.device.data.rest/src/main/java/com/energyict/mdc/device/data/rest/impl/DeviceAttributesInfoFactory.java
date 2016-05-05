package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.GeoCoordinates;
import com.elster.jupiter.metering.LocationBuilder;
import com.elster.jupiter.metering.LocationTemplate;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.RestValidationBuilder;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.rest.util.properties.PropertyType;
import com.elster.jupiter.rest.util.properties.PropertyTypeInfo;
import com.elster.jupiter.rest.util.properties.PropertyValueInfo;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.geo.Latitude;
import com.elster.jupiter.util.geo.SpatialCoordinates;
import com.elster.jupiter.validation.rest.BasicPropertyTypes;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.CIMLifecycleDates;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.rest.impl.DeviceAttributesInfo.DeviceAttribute;
import com.energyict.mdc.device.lifecycle.config.DefaultState;


import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class DeviceAttributesInfoFactory {
    private final BatchService batchService;
    private final MeteringService meteringService;
    private final Thesaurus thesaurus;
    private final ThreadPrincipalService threadPrincipalService;

    @Inject
    public DeviceAttributesInfoFactory(BatchService batchService, MeteringService meteringService, Thesaurus thesaurus, ThreadPrincipalService threadPrincipalService) {
        this.batchService = batchService;
        this.meteringService = meteringService;
        this.thesaurus = thesaurus;
        this.threadPrincipalService = threadPrincipalService;
    }

    public static String getStateName(Thesaurus thesaurus, State state) {
        Optional<DefaultState> defaultState = DefaultState.from(state);
        if (defaultState.isPresent()) {
            return thesaurus.getStringBeyondComponent(defaultState.get().getKey(), defaultState.get().getKey());
        } else {
            return state.getName();
        }
    }

    public DeviceAttributesInfo from(Device device) {
        State state = device.getState();
        DeviceAttributesInfo info = new DeviceAttributesInfo();
        Optional<Location> location = meteringService.findDeviceLocation(device.getmRID());
        Optional<GeoCoordinates> geoCoordinates = meteringService.findDeviceGeoCoordinates(device.getmRID());
        String formattedLocation = "";
        Long locationId = -1L;
        if (location.isPresent()) {
            locationId = location.get().getId();
            List<List<String>> formattedLocationMembers = meteringService.getFormattedLocationMembers(locationId);
            formattedLocationMembers.stream().skip(1).forEach(list ->
                    list.stream().filter(Objects::nonNull).findFirst().ifPresent(member -> list.set(0, "\\r\\n" + member)));
            formattedLocation = formattedLocationMembers.stream()
                    .flatMap(List::stream).filter(Objects::nonNull)
                    .collect(Collectors.joining(", "));
        }

        info.device = DeviceInfo.from(device, formattedLocation, geoCoordinates.isPresent() ? geoCoordinates.get().getCoordinates().toString() : null);

        CoordinatesInfo coordinatesInfo = new CoordinatesInfo(meteringService, device.getmRID());
        info.geoCoordinates = new DeviceAttributeInfo();
        info.geoCoordinates.displayValue = coordinatesInfo;
        fillAvailableAndEditable(info.geoCoordinates, DeviceAttribute.GEOCOORDINATES, state);

        EditLocationInfo editLocationInfo = new EditLocationInfo(meteringService, thesaurus, device.getmRID());
        info.location = new DeviceAttributeInfo();
        info.location.displayValue = editLocationInfo;
        fillAvailableAndEditable(info.location, DeviceAttribute.LOCATION, state);

        info.mrid = new DeviceAttributeInfo();
        info.mrid.displayValue = device.getmRID();
        fillAvailableAndEditable(info.mrid, DeviceAttribute.MRID, state);

        info.deviceType = new DeviceAttributeInfo();
        info.deviceType.displayValue = device.getDeviceType().getName();
        info.deviceType.attributeId = device.getDeviceType().getId();
        fillAvailableAndEditable(info.deviceType, DeviceAttribute.DEVICE_TYPE, state);

        info.deviceConfiguration = new DeviceAttributeInfo();
        info.deviceConfiguration.displayValue = device.getDeviceConfiguration().getName();
        info.deviceConfiguration.attributeId = device.getDeviceConfiguration().getId();
        fillAvailableAndEditable(info.deviceConfiguration, DeviceAttribute.DEVICE_CONFIGURATION, state);

        info.serialNumber = new DeviceAttributeInfo();
        info.serialNumber.displayValue = device.getSerialNumber();
        fillAvailableAndEditable(info.serialNumber, DeviceAttribute.SERIAL_NUMBER, state);

        info.multiplier = new DeviceAttributeInfo<>();
        info.multiplier.displayValue = device.getMultiplier();
        fillAvailableAndEditable(info.multiplier, DeviceAttribute.MULTIPLIER, state);

        info.yearOfCertification = new DeviceAttributeInfo();
        info.yearOfCertification.displayValue = device.getYearOfCertification();
        fillAvailableAndEditable(info.yearOfCertification, DeviceAttribute.YEAR_OF_CERTIFICATION, state);

        info.lifeCycleState = new DeviceAttributeInfo();
        info.lifeCycleState.displayValue = getStateName(state);
        info.lifeCycleState.attributeId = state.getId();
        fillAvailableAndEditable(info.lifeCycleState, DeviceAttribute.LIFE_CYCLE_STATE, state);

        info.batch = new DeviceAttributeInfo();
        batchService.findBatch(device).ifPresent(batch -> {
            info.batch.attributeId = batch.getId();
            info.batch.displayValue = batch.getName();
        });
        fillAvailableAndEditable(info.batch, DeviceAttribute.BATCH, state);

        info.usagePoint = new UsagePointAttributeInfo();
        device.getUsagePoint().ifPresent(usagePoint -> {
            info.usagePoint.displayValue = usagePoint.getMRID();
            info.usagePoint.mRID = usagePoint.getMRID();
            info.usagePoint.attributeId = usagePoint.getId();
        });
        fillAvailableAndEditable(info.usagePoint, DeviceAttribute.USAGE_POINT, state);

        CIMLifecycleDates lifecycleDates = device.getLifecycleDates();
        info.shipmentDate = new DeviceAttributeInfo();
        info.shipmentDate.displayValue = lifecycleDates.getReceivedDate().orElse(null);
        fillAvailableAndEditable(info.shipmentDate, DeviceAttribute.SHIPMENT_DATE, state);

        info.installationDate = new DeviceAttributeInfo();
        info.installationDate.displayValue = lifecycleDates.getInstalledDate().orElse(null);
        fillAvailableAndEditable(info.installationDate, DeviceAttribute.INSTALLATION_DATE, state);

        info.deactivationDate = new DeviceAttributeInfo();
        info.deactivationDate.displayValue = lifecycleDates.getRemovedDate().orElse(null);
        fillAvailableAndEditable(info.deactivationDate, DeviceAttribute.DEACTIVATION_DATE, state);

        info.decommissioningDate = new DeviceAttributeInfo();
        info.decommissioningDate.displayValue = lifecycleDates.getRetiredDate().orElse(null);
        fillAvailableAndEditable(info.decommissioningDate, DeviceAttribute.DECOMMISSIONING_DATE, state);

        return info;
    }

    private void fillAvailableAndEditable(DeviceAttributeInfo attribute, DeviceAttribute mapping, State state) {
        attribute.available = mapping.isAvailableForState(state);
        attribute.editable = mapping.isEditableForState(state);
    }

    private String getStateName(State state) {
        return getStateName(thesaurus, state);
    }

    public void validateOn(Device device, DeviceAttributesInfo info) {
        State currentState = device.getState();
        RestValidationBuilder validationBuilder = new RestValidationBuilder();
        if (DeviceAttributesInfo.DeviceAttribute.SHIPMENT_DATE.isEditableForState(currentState)) {
            validationBuilder.notEmpty(info.shipmentDate.displayValue, "shipmentDate");
        }
        if (DeviceAttributesInfo.DeviceAttribute.INSTALLATION_DATE.isEditableForState(currentState)) {
            validateDate(validationBuilder, "installationDate", info.getInstallationDate(), info.getShipmentDate(), DefaultTranslationKey.CIM_DATE_RECEIVE);
        }
        if (DeviceAttributesInfo.DeviceAttribute.DEACTIVATION_DATE.isEditableForState(currentState)) {
            validateDate(validationBuilder, "deactivationDate", info.getDeactivationDate(), info.getInstallationDate(), DefaultTranslationKey.CIM_DATE_INSTALLED);
        }
        if (DeviceAttributesInfo.DeviceAttribute.DECOMMISSIONING_DATE.isEditableForState(currentState)) {
            validateDate(validationBuilder, "decommissioningDate", info.getDecommissioningDate(), info.getDeactivationDate(), DefaultTranslationKey.CIM_DATE_REMOVE);
        }
        if (DeviceAttribute.GEOCOORDINATES.isEditableForState(currentState)){
            validateGeoCoordinates(validationBuilder, "geoCoordinates", info.geoCoordinates);
        }

        if (DeviceAttribute.GEOCOORDINATES.isEditableForState(currentState)){
            validateLocation(validationBuilder, "editLocation", info.location);
        }
        validationBuilder.validate();
    }

    private void validateDate(RestValidationBuilder validationBuilder, String fieldName, Optional<Instant> currentDate, Optional<Instant> previousDate, DefaultTranslationKey cimDateTranslation) {
        if (!currentDate.isPresent()) {
            validationBuilder.addValidationError(new LocalizedFieldValidationException(MessageSeeds.THIS_FIELD_IS_REQUIRED, fieldName));
        } else {
            if (previousDate.isPresent() && !currentDate.get().isAfter(previousDate.get())) {
                validationBuilder.addValidationError(new LocalizedFieldValidationException(MessageSeeds.CIM_DATE_SHOULD_BE_AFTER_X, fieldName,
                        thesaurus.getString(cimDateTranslation.getKey(), cimDateTranslation.getDefaultFormat())));
            }
        }
    }


    private void validateLocation(RestValidationBuilder validationBuilder, String fieldName, DeviceAttributeInfo<EditLocationInfo> editLocation) {
        if (editLocation.displayValue.properties != null) {
            List<PropertyInfo> propertyInfos = Arrays.asList(editLocation.displayValue.properties);
            for (PropertyInfo propertyInfo : propertyInfos) {
                if (propertyInfo.required && ((propertyInfo.propertyValueInfo.value == null) || (propertyInfo.propertyValueInfo.value.toString().length() == 0))) {
                    validationBuilder.addValidationError(new LocalizedFieldValidationException(MessageSeeds.THIS_FIELD_IS_REQUIRED, "properties." + propertyInfo.key));
                }
            }
        }
    }

    private void validateGeoCoordinates(RestValidationBuilder validationBuilder, String fieldName, DeviceAttributeInfo<CoordinatesInfo> geoCoordinates) {
        String spatialCoordinates = geoCoordinates.displayValue.spatialCoordinates;
        if (spatialCoordinates == null || spatialCoordinates.length() == 0 || spatialCoordinates.indexOf(":") == -1) {
            return;
        }
        String[] parts = spatialCoordinates.split(":");
        if (parts.length == 0) {
            return;
        }

        if (parts.length != 3) {
            validationBuilder.addValidationError(new LocalizedFieldValidationException(MessageSeeds.INVALID_COORDINATES, fieldName));
            return;
        }

        if (Arrays.asList(parts)
                .stream()
                .anyMatch(element -> element.split(",").length > 2
                        || element.split(".").length > 2)) {
            validationBuilder.addValidationError(new LocalizedFieldValidationException(MessageSeeds.INVALID_COORDINATES, fieldName));
            return;
        }

        try {
            BigDecimal numericLatitude = new BigDecimal(parts[0].contains(",") ? String.valueOf(parts[0].replace(",", ".")) : parts[0]);
            BigDecimal numericLongitude = new BigDecimal(parts[1].contains(",") ? String.valueOf(parts[1].replace(",", ".")) : parts[1]);
            BigDecimal numericElevation = new BigDecimal(parts[2]);
            if (numericLatitude.compareTo(BigDecimal.valueOf(-90)) < 0
                    || numericLatitude.compareTo(BigDecimal.valueOf(90)) > 0
                    || numericLongitude.compareTo(BigDecimal.valueOf(-180)) < 0
                    || numericLongitude.compareTo(BigDecimal.valueOf(180)) > 0) {
                validationBuilder.addValidationError(new LocalizedFieldValidationException(MessageSeeds.INVALID_COORDINATES, fieldName));
            }
        }
        catch(Exception e){
            validationBuilder.addValidationError(new LocalizedFieldValidationException(MessageSeeds.INVALID_COORDINATES, fieldName));
        }
    }

    public void writeTo(Device device, DeviceAttributesInfo info) {
        State state = device.getState();
        if (DeviceAttribute.SERIAL_NUMBER.isEditableForState(state) && info.serialNumber != null) {
            device.setSerialNumber(info.serialNumber.displayValue);
        }
        if (DeviceAttribute.YEAR_OF_CERTIFICATION.isEditableForState(state) && info.yearOfCertification != null) {
            device.setYearOfCertification(info.yearOfCertification.displayValue);
        }
        if (DeviceAttribute.BATCH.isEditableForState(state) && info.batch != null) {
            if (Checks.is(info.batch.displayValue).emptyOrOnlyWhiteSpace()) {
                batchService.findBatch(device).ifPresent(batch -> batch.removeDevice(device));
            } else {
                batchService.findOrCreateBatch(info.batch.displayValue).addDevice(device);
            }
        }
        if (DeviceAttribute.MULTIPLIER.isEditableForState(state) && info.multiplier != null) {
            device.setMultiplier(info.multiplier.displayValue);
        }
        if (DeviceAttribute.USAGE_POINT.isEditableForState(state)) {
            if (info.usagePoint != null) {
                meteringService.findUsagePoint(info.usagePoint.attributeId)
                        .ifPresent(usagePoint -> device.setUsagePoint(usagePoint));
            }
        }

        if (DeviceAttribute.MRID.isEditableForState(state) && !Objects.equals(info.mrid.displayValue, device.getmRID())) {
            device.setmRID(info.mrid.displayValue);
        }

        CIMLifecycleDates lifecycleDates = device.getLifecycleDates();
        if (DeviceAttribute.SHIPMENT_DATE.isEditableForState(state) && info.shipmentDate != null) {
            lifecycleDates.setReceivedDate(info.shipmentDate.displayValue);
        }
        if (DeviceAttribute.INSTALLATION_DATE.isEditableForState(state) && info.installationDate != null) {
            lifecycleDates.setInstalledDate(info.installationDate.displayValue);
        }
        if (DeviceAttribute.DEACTIVATION_DATE.isEditableForState(state) && info.deactivationDate != null) {
            lifecycleDates.setRemovedDate(info.deactivationDate.displayValue);
        }
        if (DeviceAttribute.DECOMMISSIONING_DATE.isEditableForState(state) && info.decommissioningDate != null) {
            lifecycleDates.setRetiredDate(info.decommissioningDate.displayValue);
        }
        lifecycleDates.save();

        if (DeviceAttribute.GEOCOORDINATES.isEditableForState(state) && info.geoCoordinates != null) {
            device.setGeoCoordintes(info.geoCoordinates.displayValue.spatialCoordinates == null? null:meteringService.createGeoCoordinates(info.geoCoordinates.displayValue.spatialCoordinates));
        }

        if (DeviceAttribute.LOCATION.isEditableForState(state) && info.location != null) {
            if ((info.location.displayValue.locationId == -1) && (info.location.displayValue.properties.length >0)){
                List<PropertyInfo> propertyInfoList = Arrays.asList(info.location.displayValue.properties);
                List<String> locationData = propertyInfoList.stream()
                        .map(d -> d.propertyValueInfo.value.toString())
                        .collect(Collectors.toList());
                LocationBuilder builder = meteringService.newLocationBuilder();
                Map<String, Integer> ranking = meteringService
                        .getLocationTemplate()
                        .getTemplateMembers()
                        .stream()
                        .collect(Collectors.toMap(LocationTemplate.TemplateField::getName,
                                LocationTemplate.TemplateField::getRanking));
                Optional<LocationBuilder.LocationMemberBuilder> memberBuilder = builder.getMember(threadPrincipalService.getLocale().getLanguage());
                if (memberBuilder.isPresent()) {
                    setLocationAttributes(memberBuilder.get(), locationData, ranking);
                } else {
                    setLocationAttributes(builder.member(), locationData, ranking).add();
                }
                device.setLocation(builder.create());

            }
            else if (info.location.displayValue.locationId > 0){
                meteringService.findLocation(info.location.displayValue.locationId).ifPresent(loc ->  device.setLocation(loc));
            }

        }
        device.save();
    }

    private LocationBuilder.LocationMemberBuilder setLocationAttributes(LocationBuilder.LocationMemberBuilder builder, List<String> locationData, Map<String, Integer> ranking) {
        builder.setCountryCode(locationData.get(ranking.get("countryCode")))
                .setCountryName(locationData.get(ranking.get("countryName")))
                .setAdministrativeArea(locationData.get(ranking.get("administrativeArea")))
                .setLocality(locationData.get(ranking.get("locality")))
                .setSubLocality(locationData.get(ranking.get("subLocality")))
                .setStreetType(locationData.get(ranking.get("streetType")))
                .setStreetName(locationData.get(ranking.get("streetName")))
                .setStreetNumber(locationData.get(ranking.get("streetNumber")))
                .setEstablishmentType(locationData.get(ranking.get("establishmentType")))
                .setEstablishmentName(locationData.get(ranking.get("establishmentName")))
                .setEstablishmentNumber(locationData.get(ranking.get("establishmentNumber")))
                .setAddressDetail(locationData.get(ranking.get("addressDetail")))
                .setZipCode(locationData.get(ranking.get("zipCode")))
                .isDaultLocation(true)
                .setLocale(threadPrincipalService.getLocale().getLanguage());

        return builder;
    }
}
