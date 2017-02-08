/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.ValidationRuleSetInfo;
import com.elster.jupiter.validation.security.Privileges;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.NumericalRegister;
import com.energyict.mdc.device.data.exceptions.InvalidLastCheckedException;
import com.energyict.mdc.device.data.rest.DeviceStatesRestricted;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

import com.google.common.collect.Range;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DeviceValidationResource {
    private final ResourceHelper resourceHelper;
    private final ValidationService validationService;
    private final ExceptionFactory exceptionFactory;
    private final Clock clock;
    private final ValidationInfoFactory validationInfoFactory;

    @Inject
    public DeviceValidationResource(ResourceHelper resourceHelper, ValidationService validationService, ExceptionFactory exceptionFactory, Clock clock, ValidationInfoFactory validationInfoFactory) {
        this.resourceHelper = resourceHelper;
        this.validationService = validationService;
        this.exceptionFactory = exceptionFactory;
        this.clock = clock;
        this.validationInfoFactory = validationInfoFactory;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION,
            Privileges.Constants.VIEW_VALIDATION_CONFIGURATION,
            Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response getValidationRuleSetsForDevice(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        List<DeviceValidationRuleSetInfo> result = new ArrayList<>();
        Optional<? extends MeterActivation> activation = device.getCurrentMeterActivation();
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        List<ValidationRuleSet> linkedRuleSets = deviceConfiguration.getValidationRuleSets();
        fillValidationRuleSetStatus(linkedRuleSets, activation, result, device);
        Collections.sort(result, ValidationRuleSetInfo.VALIDATION_RULESET_NAME_COMPARATOR);
        return Response.ok(PagedInfoList.fromPagedList("rulesets",
                ListPager.of(result).from(queryParameters).find(), queryParameters)).build();
    }

    private void fillValidationRuleSetStatus(List<ValidationRuleSet> linkedRuleSets, Optional<? extends MeterActivation> activation, List<DeviceValidationRuleSetInfo> result, Device device) {
        List<? extends ValidationRuleSet> activeRuleSets = activation
                .map(MeterActivation::getChannelsContainer)
                .map(validationService::activeRuleSets)
                .orElse(Collections.emptyList());
        for (ValidationRuleSet ruleSet : linkedRuleSets) {
            boolean isActive = !activation.isPresent() || activeRuleSets.contains(ruleSet);
            result.add(new DeviceValidationRuleSetInfo(ruleSet, device, isActive));
        }
    }

    @Path("/{validationRuleSetId}/status")
    @PUT @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION,
            Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    @DeviceStatesRestricted({DefaultState.DECOMMISSIONED})
    public Response setValidationRuleSetStatusOnDevice(@PathParam("name") String name, @PathParam("validationRuleSetId") long validationRuleSetId, DeviceValidationRuleSetInfo info) {
        info.device.name = name;
        Device device = resourceHelper.lockDeviceOrThrowException(info.device);
        ValidationRuleSet ruleSet = getValidationRuleSet(validationRuleSetId);
        MeterActivation activation = device.getCurrentMeterActivation()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.DEACTIVATE_VALIDATION_RULE_SET_NOT_POSSIBLE, ruleSet.getName()));
        setValidationRuleSetActivationStatus(activation, ruleSet, info.isActive);
        device.save();
        return Response.ok(new DeviceValidationRuleSetInfo(ruleSet, device, info.isActive)).build();
    }

    private void setValidationRuleSetActivationStatus(MeterActivation activation, ValidationRuleSet ruleSet, boolean status) {
        if (status) {
            validationService.activate(activation.getChannelsContainer(), ruleSet);
        } else {
            validationService.deactivate(activation.getChannelsContainer(), ruleSet);
        }
    }

    @Path("/validationstatus")
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION,
            Privileges.Constants.VIEW_VALIDATION_CONFIGURATION,
            Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response getValidationFeatureStatus(@PathParam("name") String name) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        DeviceValidationStatusInfo deviceValidationStatusInfo = determineStatus(device);
        return Response.status(Response.Status.OK).entity(deviceValidationStatusInfo).build();
    }

    @Path("/validationstatusactivation")
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION,
            Privileges.Constants.VIEW_VALIDATION_CONFIGURATION,
            Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response getValidationFeatureStatus2(@PathParam("name") String name) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        DeviceValidationStatusInfo deviceValidationStatusInfo = determineStatusForActivation(device);
        return Response.status(Response.Status.OK).entity(deviceValidationStatusInfo).build();
    }

    @Path("/validationmonitoring/configurationview")
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION,
            Privileges.Constants.VIEW_VALIDATION_CONFIGURATION,
            Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response getValidationMonitoringConfigurationView(@PathParam("name") String name, @QueryParam("filter") JsonQueryFilter filter) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        DeviceValidation deviceValidation = device.forValidation();
        ValidationStatusInfo validationStatusInfo =
                new ValidationStatusInfo(
                        deviceValidation.isValidationActive(),
                        deviceValidation.getLastChecked(),
                        device.hasData());
        List<DataValidationStatus> lpStatuses = new ArrayList<>();
        if(filter.hasProperty("intervalRegisterStart")
                && filter.hasProperty("intervalRegisterEnd")) {
            ValidationInfoParser parser = new ValidationInfoParser();
            List<ValidationLoadProfilePeriodInfo> loadProfilePeriodInfos = filter.getPropertyList("intervalLoadProfile", parser::parseFromNode);
            loadProfilePeriodInfos.forEach(lpPeriod -> {

                Range<Instant> intervalLP = Range.openClosed(Instant.ofEpochMilli(lpPeriod.startInterval), Instant.ofEpochMilli(lpPeriod.endInterval));
                lpStatuses.addAll(device.getLoadProfiles().stream()
                        .filter(loadProfile -> lpPeriod.id.equals(loadProfile.getId()))
                        .flatMap(l -> l.getChannels().stream())
                        .flatMap(c -> c.getDevice().forValidation().getValidationStatus(c, Collections.emptyList(), intervalLP).stream())
                        .filter(s -> (s.getReadingQualities().stream().anyMatch(q -> q.getType().qualityIndex().orElse(QualityCodeIndex.DATAVALID).equals(QualityCodeIndex.SUSPECT))))
                        .collect(Collectors.toList()));
            });
        }
        Range<Instant> intervalReg = Range.openClosed(Instant.ofEpochMilli(filter.getLong("intervalRegisterStart")), Instant.ofEpochMilli(filter.getLong("intervalRegisterEnd")));

        List<DataValidationStatus> rgStatuses = device.getRegisters().stream()
                .flatMap(r -> device.forValidation().getValidationStatus(r, Collections.emptyList(), intervalReg).stream())
                .filter(s -> (s.getReadingQualities().stream().anyMatch(q -> q.getType().qualityIndex().orElse(QualityCodeIndex.DATAVALID).equals(QualityCodeIndex.SUSPECT))))
                .collect(Collectors.toList());

        validationStatusInfo.allDataValidated = isAllDataValidated(device);

        List<DataValidationStatus> statuses = new ArrayList<>();
        statuses.addAll(lpStatuses);
        statuses.addAll(rgStatuses);

        MonitorValidationInfo info = validationInfoFactory.createMonitorValidationInfoForValidationStatues(statuses, validationStatusInfo);

        return Response.status(Response.Status.OK).entity(info).build();
    }

    @Path("/validationmonitoring/dataview")
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION,
            Privileges.Constants.VIEW_VALIDATION_CONFIGURATION,
            Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response getValidationMonitoringDataView(@PathParam("name") String name, @QueryParam("filter") JsonQueryFilter filter) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        DeviceValidation deviceValidation = device.forValidation();
        ValidationStatusInfo validationStatusInfo =
                new ValidationStatusInfo(
                        deviceValidation.isValidationActive(),
                        deviceValidation.getLastChecked(),
                        device.hasData());
        Map<LoadProfile, List<DataValidationStatus>> loadProfileStatus = new HashMap<>();
        if (filter.hasProperty("intervalRegisterStart") && filter.hasProperty("intervalRegisterEnd")) {
            ValidationInfoParser parser = new ValidationInfoParser();
            List<ValidationLoadProfilePeriodInfo> loadProfilePeriodInfos = filter.getPropertyList("intervalLoadProfile", parser::parseFromNode);
            loadProfilePeriodInfos.forEach(lpPeriod -> {

                Range<Instant> intervalLP = Range.openClosed(Instant.ofEpochMilli(lpPeriod.startInterval), Instant.ofEpochMilli(lpPeriod.endInterval));

                loadProfileStatus.putAll(device.getLoadProfiles().stream()
                        .filter(loadProfile -> lpPeriod.id.equals(loadProfile.getId()))
                        .collect(Collectors.toMap(
                                l -> l,
                                lp ->
                                        lp.getChannels().stream()
                                                .flatMap(c -> c.getDevice().forValidation().getValidationStatus(c, Collections.emptyList(), intervalLP).stream())
                                                .filter(s -> (Stream.concat(s.getReadingQualities().stream(), s.getBulkReadingQualities().stream()).anyMatch(q -> q.getType().isSuspect())))
                                                .collect(Collectors.toList())
                        )).entrySet().stream().filter(m -> !m.getValue().isEmpty())
                        .collect(Collectors.toMap(m -> (LoadProfile) (m.getKey()), m -> (List<DataValidationStatus>) (m.getValue()))));
            });
        }
        Range<Instant> intervalReg = Range.openClosed(Instant.ofEpochMilli(filter.getLong("intervalRegisterStart")), Instant.ofEpochMilli(filter.getLong("intervalRegisterEnd")));

        Map<NumericalRegister, List<DataValidationStatus>> registerStatus = device.getRegisters().stream()
                .collect(Collectors.toMap(
                        r -> r,
                        reg -> (device.forValidation().getValidationStatus(reg, Collections.emptyList(), intervalReg).stream())
                                .filter(s -> (s.getReadingQualities().stream().anyMatch(q -> q.getType().qualityIndex().orElse(QualityCodeIndex.DATAVALID).equals(QualityCodeIndex.SUSPECT))))
                                .collect(Collectors.toList())
                )).entrySet().stream().filter(m -> !m.getValue().isEmpty())
                .collect(Collectors.toMap(m -> (NumericalRegister) (m.getKey()), m -> (List<DataValidationStatus>) (m.getValue())));

        validationStatusInfo.allDataValidated = isAllDataValidated(device);

        MonitorValidationInfo info = validationInfoFactory.createMonitorValidationInfoForLoadProfileAndRegister(loadProfileStatus, registerStatus, validationStatusInfo);

        return Response.status(Response.Status.OK).entity(info).build();
    }

    @Path("/validationmonitoring/register")
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION,
            Privileges.Constants.VIEW_VALIDATION_CONFIGURATION,
            Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response getValidationMonitoringRegister(@PathParam("name") String name) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        ZonedDateTime end = ZonedDateTime.ofInstant(clock.instant(), clock.getZone()).truncatedTo(ChronoUnit.DAYS).plusDays(1);

        ZonedDateTime intervalStart = end.minusYears(1);
        Range<Instant> interval = Range.openClosed(intervalStart.toInstant(), end.toInstant());

        Map<NumericalRegister, Long> registerStatus = device.getRegisters().stream()
                .collect(Collectors.toMap(
                        r -> r,
                        reg -> (device.forValidation().getValidationStatus(reg, Collections.emptyList(), interval).stream())
                                .collect(Collectors.counting())
                )).entrySet().stream().filter(m -> m.getValue() > 0L)
                .collect(Collectors.toMap(m -> (NumericalRegister) (m.getKey()), m -> (Long) (m.getValue())));

        return Response.status(Response.Status.OK).entity(registerStatus).build();
    }

    private DeviceValidationStatusInfo determineStatus(Device device) {
        DeviceValidation deviceValidation = device.forValidation();
        Optional<Instant> lastChecked = Optional.empty();
        if(deviceValidation.getLastValidationRun().isPresent()){
            lastChecked = deviceValidation.getLastValidationRun().equals(Optional.of(device.getMeterActivationsMostRecentFirst().get(0).getStart())) ? Optional.empty() : deviceValidation.getLastValidationRun();
        }
        DeviceValidationStatusInfo deviceValidationStatusInfo =
                new DeviceValidationStatusInfo(
                        deviceValidation.isValidationActive(),
                        deviceValidation.isValidationOnStorage(),
                        lastChecked,
                        device.hasData());

        ZonedDateTime end = ZonedDateTime.ofInstant(clock.instant(), clock.getZone()).truncatedTo(ChronoUnit.DAYS).plusDays(1);

        collectRegisterData(device, deviceValidationStatusInfo, end);
        collectLoadProfileData(device, deviceValidationStatusInfo, end);
        deviceValidationStatusInfo.device = DeviceInfo.from(device);

        return deviceValidationStatusInfo;
    }

    private DeviceValidationStatusInfo determineStatusForActivation(Device device) {
        DeviceValidation deviceValidation = device.forValidation();
        DeviceValidationStatusInfo deviceValidationStatusInfo =
                new DeviceValidationStatusInfo(
                        deviceValidation.isValidationActive(),
                        deviceValidation.isValidationOnStorage(),
                        deviceValidation.getLastChecked(),
                        device.hasData());

        ZonedDateTime end = ZonedDateTime.ofInstant(clock.instant(), clock.getZone()).truncatedTo(ChronoUnit.DAYS).plusDays(1);

        collectRegisterData(device, deviceValidationStatusInfo, end);
        collectLoadProfileData(device, deviceValidationStatusInfo, end);
        deviceValidationStatusInfo.device = DeviceInfo.from(device);

        return deviceValidationStatusInfo;
    }

    private void collectLoadProfileData(Device device, DeviceValidationStatusInfo deviceValidationStatusInfo, ZonedDateTime end) {
        ZonedDateTime loadProfileStart = end.minusMonths(1);
        Range<Instant> loadProfileInterval = Range.openClosed(loadProfileStart.toInstant(), end.toInstant());

        List<DataValidationStatus> statuses = device.getLoadProfiles().stream()
                .flatMap(l -> l.getChannels().stream())
                .flatMap(c -> c.getDevice().forValidation().getValidationStatus(c, Collections.emptyList(), loadProfileInterval).stream())
                .collect(Collectors.toList());

        deviceValidationStatusInfo.loadProfileSuspectCount = statuses.stream()
                .flatMap(d -> d.getReadingQualities().stream())
                .filter(r -> QualityCodeIndex.SUSPECT.equals(r.getType().qualityIndex().orElse(null)))
                .count();
        if (statuses.isEmpty()) {
            deviceValidationStatusInfo.allDataValidated &= device.getRegisters().stream()
                    .allMatch(r -> r.getDevice().forValidation().allDataValidated(r, clock.instant()));
        } else {
            deviceValidationStatusInfo.allDataValidated &= statuses.stream()
                    .allMatch(DataValidationStatus::completelyValidated);
        }
    }

    private void collectRegisterData(Device device, DeviceValidationStatusInfo deviceValidationStatusInfo, ZonedDateTime end) {
        ZonedDateTime registerStart = end.minusYears(1);
        Range<Instant> registerRange = Range.openClosed(registerStart.toInstant(), end.toInstant());

        List<DataValidationStatus> statuses = device.getRegisters().stream()
                .flatMap(r -> device.forValidation().getValidationStatus(r, Collections.emptyList(), registerRange).stream())
                .collect(Collectors.toList());

        deviceValidationStatusInfo.registerSuspectCount = statuses.stream()
                .flatMap(d -> d.getReadingQualities().stream())
                .filter(r -> QualityCodeIndex.SUSPECT.equals(r.getType().qualityIndex().orElse(null)))
                .count();
        if (statuses.isEmpty()) {
            deviceValidationStatusInfo.allDataValidated &= device.getLoadProfiles().stream()
                    .flatMap(l -> l.getChannels().stream())
                    .allMatch(r -> r.getDevice().forValidation().allDataValidated(r, clock.instant()));
        } else {
            deviceValidationStatusInfo.allDataValidated &= statuses.stream()
                    .allMatch(DataValidationStatus::completelyValidated);
        }
    }

    @Path("/validationstatus")
    @PUT @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION,
            Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    @DeviceStatesRestricted({DefaultState.IN_STOCK, DefaultState.DECOMMISSIONED})
    public Response setValidationFeatureStatus(@PathParam("name") String name, DeviceValidationStatusInfo info) {
        info.device.name = name;
        Device device = resourceHelper.lockDeviceOrThrowException(info.device);
        try {
            if (info.isActive) {
                if (info.lastChecked == null) {
                    throw new LocalizedFieldValidationException(MessageSeeds.NULL_DATE, "lastChecked");
                }
                if(info.isStorage){
                    device.forValidation().activateValidationOnStorage(Instant.ofEpochMilli(info.lastChecked));
                } else{
                    device.forValidation().activateValidation(Instant.ofEpochMilli(info.lastChecked));
                }
            } else {
                device.forValidation().deactivateValidation();
            }
            device.save();
        } catch (InvalidLastCheckedException e) {
            throw new LocalizedFieldValidationException(e.getMessageSeed(), "lastChecked", device.getName(), e.getOldLastChecked(), e.getNewLastChecked());
        }
        return Response.status(Response.Status.OK).build();
    }

    @Path("/validate")
    @PUT @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.VALIDATE_MANUAL)
    @DeviceStatesRestricted({DefaultState.IN_STOCK, DefaultState.DECOMMISSIONED})
    public Response validateDeviceData(@PathParam("name") String name, DeviceInfo info) {
        info.name = name;
        Device device = resourceHelper.lockDeviceOrThrowException(info);
        device.forValidation().validateData();
        device.save();
        return Response.status(Response.Status.OK).build();
    }

    private ValidationRuleSet getValidationRuleSet(long validationRuleSetId) {
        Optional<? extends ValidationRuleSet> ruleSet = validationService.getValidationRuleSet(validationRuleSetId);
        return ruleSet.orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private boolean isAllDataValidated(Device device) {
        boolean result = true;

        ZonedDateTime end = ZonedDateTime.ofInstant(clock.instant(), clock.getZone()).truncatedTo(ChronoUnit.DAYS).plusDays(1);

        Range<Instant> loadProfileRange = Range.openClosed(end.minusMonths(1).toInstant(), end.toInstant());

        List<DataValidationStatus> lpStatuses = device.getLoadProfiles().stream()
                .flatMap(l -> l.getChannels().stream())
                .flatMap(c -> c.getDevice().forValidation().getValidationStatus(c, Collections.emptyList(), loadProfileRange).stream())
                .collect(Collectors.toList());

            result &= device.getLoadProfiles().stream()
                    .flatMap(l -> l.getChannels().stream())
                    .allMatch(r -> r.getDevice().forValidation().allDataValidated(r, clock.instant()));

            result &= lpStatuses.stream()
                    .allMatch(DataValidationStatus::completelyValidated);

        Range<Instant> registerRange = Range.openClosed(end.minusYears(1).toInstant(), end.toInstant());

        List<DataValidationStatus> rgStatuses = device.getRegisters().stream()
                .flatMap(r -> device.forValidation().getValidationStatus(r, Collections.emptyList(), registerRange).stream())
                .collect(Collectors.toList());

            result &= device.getRegisters().stream()
                    .allMatch(r -> r.getDevice().forValidation().allDataValidated(r, clock.instant()));

            result &= rgStatuses.stream()
                    .allMatch(DataValidationStatus::completelyValidated);

        return result;
    }

}
