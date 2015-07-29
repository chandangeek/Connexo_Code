package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.ValidationRuleSetInfo;
import com.elster.jupiter.validation.security.Privileges;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.JsonQueryParameters;
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
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

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

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION,Privileges.VIEW_VALIDATION_CONFIGURATION,com.elster.jupiter.validation.security.Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response getValidationRuleSetsForDevice(@PathParam("mRID") String mRID, @BeanParam JsonQueryParameters queryParameters) {
        List<DeviceValidationRuleSetInfo> result = new ArrayList<>();
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        Optional<? extends MeterActivation> activation = device.getCurrentMeterActivation();
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        List<ValidationRuleSet> linkedRuleSets = deviceConfiguration.getValidationRuleSets();
        fillValidationRuleSetStatus(linkedRuleSets, activation, result);
        Collections.sort(result, ValidationRuleSetInfo.VALIDATION_RULESET_NAME_COMPARATOR);
        return Response.ok(PagedInfoList.fromPagedList("rulesets",
                ListPager.of(result).from(queryParameters).find(), queryParameters)).build();
    }

    private void fillValidationRuleSetStatus(List<ValidationRuleSet> linkedRuleSets, Optional<? extends MeterActivation> activation, List<DeviceValidationRuleSetInfo> result) {
        List<? extends ValidationRuleSet> activeRuleSets = activation.map(validationService::activeRuleSets).orElse(Collections.emptyList());
        linkedRuleSets.forEach(ruleSet -> result.add(new DeviceValidationRuleSetInfo(ruleSet, (!activation.isPresent()) || activeRuleSets.contains(ruleSet))));
    }

    @Path("/{validationRuleSetId}/status")
    @PUT
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION,com.elster.jupiter.validation.security.Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    @DeviceStatesRestricted({DefaultState.DECOMMISSIONED})
    public Response setValidationRuleSetStatusOnDevice(@PathParam("mRID") String mRID, @PathParam("validationRuleSetId") long validationRuleSetId, boolean status) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        ValidationRuleSet ruleSet = getValidationRuleSet(validationRuleSetId);
        Optional<? extends MeterActivation> activation = device.getCurrentMeterActivation();
        if (activation.isPresent()) {
            setValidationRuleSetActivationStatus(activation.get(), ruleSet, status);
        } else {
            throw exceptionFactory.newException(MessageSeeds.DEACTIVATE_VALIDATION_RULE_SET_NOT_POSSIBLE, ruleSet.getName());
        }
        return Response.status(Response.Status.OK).build();
    }

    private void setValidationRuleSetActivationStatus(MeterActivation activation, ValidationRuleSet ruleSet, boolean status) {
      if (status) {
    	  validationService.activate(activation, ruleSet);
      } else {
    	  validationService.deactivate(activation, ruleSet);
      }
    }

    @Path("/validationstatus")
    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION,Privileges.VIEW_VALIDATION_CONFIGURATION,com.elster.jupiter.validation.security.Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response getValidationFeatureStatus(@PathParam("mRID") String mRID) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);

        DeviceValidationStatusInfo deviceValidationStatusInfo = determineStatus(device);
        return Response.status(Response.Status.OK).entity(deviceValidationStatusInfo).build();
    }

    @Path("/validationmonitoring/configurationview")
    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.VIEW_VALIDATION_CONFIGURATION, com.elster.jupiter.validation.security.Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response getValidationMonitoringConfigurationView(@PathParam("mRID") String mRID,
                                            @QueryParam("intervalRegisterStart") Long intervalStart,
                                            @QueryParam("intervalRegisterEnd") Long intervalEnd,
                                            @QueryParam("intervalLoadProfile") ValidationLoadProfilePeriodsInfo intervalLoadProfile) {

        List<DataValidationStatus> lpStatuses = new ArrayList<>();

        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        DeviceValidation deviceValidation = device.forValidation();
        ValidationStatusInfo validationStatusInfo =
                new ValidationStatusInfo(
                        deviceValidation.isValidationActive(),
                        deviceValidation.getLastChecked(),
                        device.hasData());

        intervalLoadProfile.getLoadProfilePeriodInfos().stream().forEach(lpPeriod -> {

            Range<Instant> intervalLP = Range.openClosed(Instant.ofEpochMilli(lpPeriod.startInterval), Instant.ofEpochMilli(lpPeriod.endInterval));
            lpStatuses.addAll(device.getLoadProfiles().stream()
                    .filter(loadProfile -> lpPeriod.id.equals(loadProfile.getId()))
                    .flatMap(l -> l.getChannels().stream())
                    .flatMap(c -> c.getDevice().forValidation().getValidationStatus(c, Collections.emptyList(), intervalLP).stream())
                    .collect(Collectors.toList()));
        });
        Range<Instant> intervalReg = Range.openClosed(Instant.ofEpochMilli(intervalStart), Instant.ofEpochMilli(intervalEnd));

        List<DataValidationStatus> rgStatuses = device.getRegisters().stream()
                .flatMap(r -> device.forValidation().getValidationStatus(r, Collections.emptyList(), intervalReg).stream())
                .collect(Collectors.toList());

        validationStatusInfo.allDataValidated = isAllDataValidated(lpStatuses, rgStatuses, device);

        List<DataValidationStatus> statuses = new ArrayList<>();
        statuses.addAll(lpStatuses);
        statuses.addAll(rgStatuses);

        MonitorValidationInfo info = validationInfoFactory.createMonitorValidationInfoForValidationStatues(statuses, validationStatusInfo);

        return Response.status(Response.Status.OK).entity(info).build();
    }

    @Path("/validationmonitoring/dataview")
    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.VIEW_VALIDATION_CONFIGURATION, com.elster.jupiter.validation.security.Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response getValidationMonitoringDataView(@PathParam("mRID") String mRID,
                                                       @QueryParam("intervalRegisterStart") Long intervalStart,
                                                       @QueryParam("intervalRegisterEnd") Long intervalEnd,
                                                       @QueryParam("intervalLoadProfile") ValidationLoadProfilePeriodsInfo intervalLoadProfile) {

        Map<LoadProfile, List<DataValidationStatus>> loadProfileStatus = new HashMap<>();

        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);

        DeviceValidation deviceValidation = device.forValidation();
        ValidationStatusInfo validationStatusInfo =
                new ValidationStatusInfo(
                        deviceValidation.isValidationActive(),
                        deviceValidation.getLastChecked(),
                        device.hasData());

        intervalLoadProfile.getLoadProfilePeriodInfos().stream().forEach(lpPeriod -> {

            Range<Instant> intervalLP = Range.openClosed(Instant.ofEpochMilli(lpPeriod.startInterval), Instant.ofEpochMilli(lpPeriod.endInterval));

            loadProfileStatus.putAll(device.getLoadProfiles().stream()
                    .filter(loadProfile -> lpPeriod.id.equals(loadProfile.getId()))
                    .collect(Collectors.toMap(
                            l -> l,
                            lp ->
                                    lp.getChannels().stream()
                                            .flatMap(c -> c.getDevice().forValidation().getValidationStatus(c, Collections.emptyList(), intervalLP).stream())
                                            .collect(Collectors.toList())
                    )).entrySet().stream().filter(m -> (((List<DataValidationStatus>) m.getValue()).size()) > 0L)
                    .collect(Collectors.toMap(m -> (LoadProfile) (m.getKey()), m -> (List<DataValidationStatus>) (m.getValue()))));
        });

        Range<Instant> intervalReg = Range.openClosed(Instant.ofEpochMilli(intervalStart), Instant.ofEpochMilli(intervalEnd));

        Map<NumericalRegister, List<DataValidationStatus>> registerStatus = device.getRegisters().stream()
                .collect(Collectors.toMap(
                        r -> r,
                        reg -> (device.forValidation().getValidationStatus(reg, Collections.emptyList(), intervalReg).stream())
                                .collect(Collectors.toList())
                )).entrySet().stream().filter(m -> (((List<DataValidationStatus>)m.getValue()).size()) > 0L)
                .collect(Collectors.toMap(m -> (NumericalRegister) (m.getKey()), m -> (List<DataValidationStatus>) (m.getValue())));


        List<DataValidationStatus> lpsList = loadProfileStatus.entrySet().stream().flatMap(lps -> lps.getValue().stream()).collect(Collectors.toList());
        List<DataValidationStatus> rsList = registerStatus.entrySet().stream().flatMap(rs -> rs.getValue().stream()).collect(Collectors.toList());

        validationStatusInfo.allDataValidated = isAllDataValidated(lpsList, rsList, device);

        MonitorValidationInfo info = validationInfoFactory.createMonitorValidationInfoForLoadProfileAndRegister(loadProfileStatus, registerStatus, validationStatusInfo);

        return Response.status(Response.Status.OK).entity(info).build();
    }

    @Path("/validationmonitoring/register")
    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION, Privileges.VIEW_VALIDATION_CONFIGURATION, com.elster.jupiter.validation.security.Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response getValidationMonitoringRegister(@PathParam("mRID") String mrid) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        ZonedDateTime end = ZonedDateTime.ofInstant(clock.instant(), clock.getZone()).truncatedTo(ChronoUnit.DAYS).plusDays(1);

        ZonedDateTime intervalStart = end.minusYears(1);
        Range<Instant> interval = Range.openClosed(intervalStart.toInstant(), end.toInstant());

        Map<NumericalRegister, Long> registerStatus = device.getRegisters().stream()
                .collect(Collectors.toMap(
                        r -> r,
                        reg -> (device.forValidation().getValidationStatus(reg, Collections.emptyList(), interval).stream())
                                .collect(Collectors.counting())
                )).entrySet().stream().filter(m -> ((Long) m.getValue()) > 0L)
                .collect(Collectors.toMap(m -> (NumericalRegister) (m.getKey()), m -> (Long) (m.getValue())));

        return Response.status(Response.Status.OK).entity(registerStatus).build();
    }

    private DeviceValidationStatusInfo determineStatus(Device device) {
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
            deviceValidationStatusInfo.allDataValidated = statuses.stream()
                    .allMatch(DataValidationStatus::completelyValidated);
        }
    }

    @Path("/validationstatus")
    @PUT
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION,com.elster.jupiter.validation.security.Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    @DeviceStatesRestricted({DefaultState.IN_STOCK, DefaultState.DECOMMISSIONED})
    public Response setValidationFeatureStatus(@PathParam("mRID") String mRID, DeviceValidationStatusInfo deviceValidationStatusInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        try {
            if (deviceValidationStatusInfo.isActive) {
                if (deviceValidationStatusInfo.lastChecked == null) {
                    throw new LocalizedFieldValidationException(MessageSeeds.NULL_DATE, "lastChecked");
                }
                if(deviceValidationStatusInfo.isStorage){
                    device.forValidation().activateValidationOnStorage(Instant.ofEpochMilli(deviceValidationStatusInfo.lastChecked));
                }
                else{
                    device.forValidation().activateValidation(Instant.ofEpochMilli(deviceValidationStatusInfo.lastChecked));
                }
            }
            else {
                device.forValidation().deactivateValidation();
            }
        }
        catch (InvalidLastCheckedException e) {
            throw new LocalizedFieldValidationException(this.toMessageSeed(e), "lastChecked", device.forValidation().getLastChecked());
        }
        return Response.status(Response.Status.OK).build();
    }

    private MessageSeed toMessageSeed(InvalidLastCheckedException e) {
        switch (e.getReason()) {
            case NULL: {
                return MessageSeeds.NULL_DATE;
            }
            case AFTER_CURRENT_LAST_CHECKED: {
                return MessageSeeds.INVALID_DATE;
            }
            default: {
                return MessageSeeds.INVALID_DATE;
            }
        }
    }

    @Path("/validate")
    @PUT
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(com.elster.jupiter.validation.security.Privileges.VALIDATE_MANUAL)
    @DeviceStatesRestricted({DefaultState.IN_STOCK, DefaultState.DECOMMISSIONED})
    public Response validateDeviceData(@PathParam("mRID") String mRID) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        device.forValidation().validateData();
        return Response.status(Response.Status.OK).build();
    }

    private ValidationRuleSet getValidationRuleSet(long validationRuleSetId) {
        Optional<? extends ValidationRuleSet> ruleSet = validationService.getValidationRuleSet(validationRuleSetId);
        return ruleSet.orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private boolean isAllDataValidated(List<DataValidationStatus> lpStatuses, List<DataValidationStatus> rgStatuses, Device device) {
        boolean result = true;
        if (lpStatuses.isEmpty()) {
            result &= device.getLoadProfiles().stream()
                    .flatMap(l -> l.getChannels().stream())
                    .allMatch(r -> r.getDevice().forValidation().allDataValidated(r, clock.instant()));
        } else {
            result &= lpStatuses.stream()
                    .allMatch(DataValidationStatus::completelyValidated);
        }

        if (rgStatuses.isEmpty()) {
            result &= device.getRegisters().stream()
                    .allMatch(r -> r.getDevice().forValidation().allDataValidated(r, clock.instant()));
        } else {
            result &= rgStatuses.stream()
                    .allMatch(DataValidationStatus::completelyValidated);
        }
        return result;
    }

}
