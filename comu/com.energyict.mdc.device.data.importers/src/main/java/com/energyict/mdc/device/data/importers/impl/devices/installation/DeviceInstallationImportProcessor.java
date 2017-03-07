/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.devices.installation;

import com.elster.jupiter.fileimport.csvimport.exceptions.ProcessorException;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.LocationBuilder;
import com.elster.jupiter.metering.LocationTemplate.TemplateField;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.geo.SpatialCoordinatesFactory;
import com.elster.jupiter.util.time.DefaultDateTimeFormatters;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.exceptions.UnsatisfiedReadingTypeRequirementsOfUsagePointException;
import com.energyict.mdc.device.data.exceptions.UsagePointAlreadyLinkedToAnotherDeviceException;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.FileImportLogger;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.TranslationKeys;
import com.energyict.mdc.device.data.importers.impl.devices.DeviceTransitionImportProcessor;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.config.DefaultCustomStateTransitionEventType;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

class DeviceInstallationImportProcessor extends DeviceTransitionImportProcessor<DeviceInstallationImportRecord> {

    private static final DateTimeFormatter timeFormatter = DefaultDateTimeFormatters.longDate().withLongTime().build().withZone(ZoneId.systemDefault());

    DeviceInstallationImportProcessor(DeviceDataImporterContext context) {
        super(context);
    }

    @Override
    protected void beforeTransition(Device device, DeviceInstallationImportRecord data) throws ProcessorException {
        List<String> locationData = data.getLocation();
        List<String> geoCoordinatesData = data.getGeoCoordinates();
        EndDevice endDevice = findEndDeviceByIdentifier(data.getDeviceIdentifier())
                .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_DEVICE, data.getLineNumber(), data.getDeviceIdentifier()));
        if (isLocationPresent(locationData)) {
            LocationBuilder builder = endDevice.getAmrSystem().newMeter(endDevice.getAmrId(), "Fake").newLocationBuilder();
            Map<String, Integer> ranking = super.getContext()
                    .getMeteringService()
                    .getLocationTemplate()
                    .getTemplateMembers()
                    .stream()
                    .collect(Collectors.toMap(TemplateField::getName,
                            TemplateField::getRanking));
            if (ranking.entrySet().size() != locationData.size()) {
                String fields = super.getContext()
                        .getMeteringService()
                        .getLocationTemplate()
                        .getTemplateMembers()
                        .stream()
                        .sorted((t1, t2) -> Integer.compare(t1.getRanking(), t2.getRanking()))
                        .map(TemplateField::getName)
                        .map(s -> s = "<" + s + ">")
                        .collect(Collectors.joining(" "));
                throw new ProcessorException(MessageSeeds.INCORRECT_LOCATION_FORMAT, fields);
            } else {
                super.getContext()
                        .getMeteringService()
                        .getLocationTemplate()
                        .getTemplateMembers()
                        .stream()
                        .filter(TemplateField::isMandatory)
                        .forEach(field -> {
                            if (locationData.get(field.getRanking()) == null) {
                                throw new ProcessorException(MessageSeeds.LINE_MISSING_LOCATION_VALUE, data.getLineNumber(), field.getName());
                            }
                        });
            }
            Optional<LocationBuilder.LocationMemberBuilder> memberBuilder = builder.getMemberBuilder(locationData
                    .get(ranking.get("locale")));
            if (memberBuilder.isPresent()) {
                setLocationAttributes(memberBuilder.get(), data, ranking);
            } else {
                setLocationAttributes(builder.member(), data, ranking).add();
            }
            endDevice.setLocation(builder.create());
        }

        if(geoCoordinatesData!=null && !geoCoordinatesData.isEmpty() && !geoCoordinatesData.contains(null)){
            endDevice.setSpatialCoordinates(new SpatialCoordinatesFactory().fromStringValue(geoCoordinatesData.stream().reduce((s, t) -> s + ":" + t).get()));
        }
        endDevice.update();
    }

    @Override
    protected void afterTransition(Device device, DeviceInstallationImportRecord data, FileImportLogger logger) throws ProcessorException {
        super.afterTransition(device, data, logger);
        processUsagePoint(device, data, logger);
    }

    protected DefaultCustomStateTransitionEventType getTransitionEventType(DeviceInstallationImportRecord data) {
        return data.isInstallInactive() ? DefaultCustomStateTransitionEventType.DEACTIVATED
                : DefaultCustomStateTransitionEventType.ACTIVATED;
    }

    @Override
    protected List<DefaultState> getSourceStates(DeviceInstallationImportRecord data) {
        return Arrays.asList(DefaultState.IN_STOCK, DefaultState.COMMISSIONING);
    }

    @Override
    protected DefaultState getTargetState(DeviceInstallationImportRecord data) {
        return data.isInstallInactive() ? DefaultState.INACTIVE : DefaultState.ACTIVE;
    }

    private void processUsagePoint(Device device, DeviceInstallationImportRecord data, FileImportLogger logger) {
        if (data.getUsagePointIdentifier() != null) {
            Optional<UsagePoint> usagePointRef = findUsagePointByIdentifier(data.getUsagePointIdentifier());
            if (usagePointRef.isPresent()) {
                setUsagePoint(device, usagePointRef.get(), data);
            } else {
                // If not found, than create the light version of the usage point using usage point name + Service category
                logger.warning(TranslationKeys.NEW_USAGE_POINT_WILL_BE_CREATED, data.getLineNumber(), data.getUsagePointIdentifier());
                setUsagePoint(device, createNewUsagePoint(data), data);
            }
        }
    }

    private UsagePoint createNewUsagePoint(DeviceInstallationImportRecord data) {
        return Arrays.stream(ServiceKind.values())
                .filter(candidate -> candidate.getDisplayName().equalsIgnoreCase(data.getServiceCategory()))
                .map(serviceKind -> getContext().getMeteringService().getServiceCategory(serviceKind))
                .findFirst()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_USAGE_POINT,
                        data.getLineNumber(),
                        data.getUsagePointIdentifier(),
                        Arrays.stream(ServiceKind.values()).map(ServiceKind::getDisplayName).collect(Collectors.joining(", "))))
                .newUsagePoint(data.getUsagePointIdentifier(), getContext().getClock().instant())
                .create();
    }

    private void setUsagePoint(Device device, UsagePoint usagePoint, DeviceInstallationImportRecord data) {
        try {
            MeterRole defaultMeterRole = getContext().getMetrologyConfigurationService().findDefaultMeterRole(DefaultMeterRole.DEFAULT);
            device.activate(data.getTransitionDate().orElse(getContext().getClock().instant()), usagePoint, defaultMeterRole);
        } catch (UsagePointAlreadyLinkedToAnotherDeviceException e) {
            rethrowAsProcessorException(e, data, usagePoint);
        } catch (UnsatisfiedReadingTypeRequirementsOfUsagePointException e) {
            rethrowAsProcessorException(e, data, device, usagePoint);
        }
    }

    private void rethrowAsProcessorException(UsagePointAlreadyLinkedToAnotherDeviceException cause, DeviceInstallationImportRecord data, UsagePoint usagePoint) {
        Instant start = cause.getMeterActivation().getStart();
        Instant end = cause.getMeterActivation().getEnd();
        if (end == null) {
            throw new ProcessorException(MessageSeeds.USAGE_POINT_ALREADY_LINKED_TO_ANOTHER_DEVICE,
                    data.getLineNumber(), usagePoint.getName(), cause.getMeterActivation().getMeter().get().getName(), getFormattedInstant(start));
        } else {
            throw new ProcessorException(MessageSeeds.USAGE_POINT_ALREADY_LINKED_TO_ANOTHER_DEVICE_UNTIL,
                    data.getLineNumber(), usagePoint.getName(), cause.getMeterActivation().getMeter().get().getName(), getFormattedInstant(start), getFormattedInstant(end));
        }
    }

    private void rethrowAsProcessorException(UnsatisfiedReadingTypeRequirementsOfUsagePointException ex, DeviceInstallationImportRecord data, Device device, UsagePoint usagePoint) {
        throw new ProcessorException(MessageSeeds.UNSATISFIED_READING_TYPE_REQUIREMENTS_OF_USAGE_POINT, data.getLineNumber(), device.getName(), usagePoint.getName(),
                UnsatisfiedReadingTypeRequirementsOfUsagePointException.buildRequirementsString(ex.getUnsatisfiedRequirements()));
    }

    static String getFormattedInstant(Instant time) {
        return timeFormatter.format(LocalDateTime.ofInstant(time, ZoneId.systemDefault()));
    }

    @Override
    protected List<ExecutableActionProperty> getExecutableActionProperties(DeviceInstallationImportRecord data, Map<String, PropertySpec> allPropertySpecsForAction, FileImportLogger logger, ExecutableAction executableAction) {
        List<ExecutableActionProperty> executableActionProperties = super.getExecutableActionProperties(data, allPropertySpecsForAction, logger, executableAction);

        if (data.getMultiplier() != null) {
            PropertySpec propertySpec = allPropertySpecsForAction.get(DeviceLifeCycleService.MicroActionPropertyName.MULTIPLIER
                    .key());
            if (propertySpec != null) {
                try {
                    executableActionProperties.add(getContext().getDeviceLifeCycleService()
                            .toExecutableActionProperty(data.getMultiplier(), propertySpec));
                } catch (InvalidValueException e) {
                    throw new ProcessorException(MessageSeeds.INCORRECT_MULTIPLIER_VALUE,
                            data.getLineNumber(), data.getMultiplier(), e.getLocalizedMessage());
                }
            } else {
                logger.warning(MessageSeeds.USELESS_MULTIPLIER_CONFIGURED, data.getLineNumber(), data.getMultiplier());

            }
        }
        return executableActionProperties;
    }

    private LocationBuilder.LocationMemberBuilder setLocationAttributes(LocationBuilder.LocationMemberBuilder builder, DeviceInstallationImportRecord data, Map<String, Integer> ranking) {
        builder.setCountryCode(data.getLocation().get(ranking.get("countryCode")))
                .setCountryName(data.getLocation().get(ranking.get("countryName")))
                .setAdministrativeArea(data.getLocation().get(ranking.get("administrativeArea")))
                .setLocality(data.getLocation().get(ranking.get("locality")))
                .setSubLocality(data.getLocation().get(ranking.get("subLocality")))
                .setStreetType(data.getLocation().get(ranking.get("streetType")))
                .setStreetName(data.getLocation().get(ranking.get("streetName")))
                .setStreetNumber(data.getLocation().get(ranking.get("streetNumber")))
                .setEstablishmentType(data.getLocation().get(ranking.get("establishmentType")))
                .setEstablishmentName(data.getLocation().get(ranking.get("establishmentName")))
                .setEstablishmentNumber(data.getLocation().get(ranking.get("establishmentNumber")))
                .setAddressDetail(data.getLocation().get(ranking.get("addressDetail")))
                .setZipCode(data.getLocation().get(ranking.get("zipCode")))
                .isDaultLocation(true)
                .setLocale(data.getLocation().get(ranking.get("locale")) != null ? data.getLocation().get(ranking.get("locale")) : "en");
        return builder;
    }

    private boolean isLocationPresent(List<String> locationData){
        return locationData != null && !locationData.isEmpty() && !locationData.stream().allMatch(location -> location == null);
    }
}
