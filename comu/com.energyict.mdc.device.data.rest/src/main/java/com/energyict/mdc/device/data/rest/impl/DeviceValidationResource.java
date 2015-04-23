package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;
import com.elster.jupiter.validation.rest.ValidationRuleSetInfo;
import com.elster.jupiter.validation.rest.ValidationRuleSetVersionInfo;
import com.elster.jupiter.validation.security.Privileges;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceValidation;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.swing.text.html.Option;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
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
    private final DeviceConfigurationService deviceConfigurationService;
    private final MeteringService meteringService;
    private final ExceptionFactory exceptionFactory;
    private final Clock clock;

    @Inject
    public DeviceValidationResource(ResourceHelper resourceHelper, ValidationService validationService, DeviceConfigurationService deviceConfigurationService, MeteringService meteringService, ExceptionFactory exceptionFactory, Clock clock) {
        this.resourceHelper = resourceHelper;
        this.validationService = validationService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.meteringService = meteringService;
        this.exceptionFactory = exceptionFactory;
        this.clock = clock;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION,Privileges.VIEW_VALIDATION_CONFIGURATION,com.elster.jupiter.validation.security.Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response getValidationRulsetsForDevice(@PathParam("mRID") String mrid, @BeanParam QueryParameters queryParameters) {
        List<DeviceValidationRuleSetInfo> result = new ArrayList<>();
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        Meter meter = getMeterFor(device);

        Optional<? extends MeterActivation> activation = meter.getCurrentMeterActivation();

        Optional<DeviceConfiguration> deviceConfig = deviceConfigurationService.findDeviceConfiguration(device.getDeviceConfiguration().getId());
        if (deviceConfig.isPresent()) {
            List<ValidationRuleSet> linkedRuleSets = deviceConfig.get().getValidationRuleSets();
            fillValidationRuleSetStatus(linkedRuleSets, activation, result);
        }
        Collections.sort(result, ValidationRuleSetInfo.VALIDATION_RULESET_NAME_COMPARATOR);
        return Response.ok(PagedInfoList.fromPagedList("rulesets",
                ListPager.of(result).from(queryParameters).find(), queryParameters)).build();
    }

    private void fillValidationRuleSetStatus(List<ValidationRuleSet> linkedRuleSets, Optional<? extends MeterActivation> activation, List<DeviceValidationRuleSetInfo> result) {
        List<? extends ValidationRuleSet> activeRuleSets = activation.map(validationService::activeRuleSets).orElse(Collections.emptyList());
        linkedRuleSets.forEach(ruleset -> result.add(new DeviceValidationRuleSetInfo(ruleset, (!activation.isPresent()) || activeRuleSets.contains(ruleset))));
    }

    @Path("/{validationRuleSetId}/status")
    @PUT
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION,com.elster.jupiter.validation.security.Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response setValidationRuleSetStatusOnDevice(@PathParam("mRID") String mrid, @PathParam("validationRuleSetId") long validationRuleSetId, boolean status) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        Meter meter = getMeterFor(device);
        ValidationRuleSet ruleSet = getValidationRuleSet(validationRuleSetId);
        Optional<? extends MeterActivation> activation = meter.getCurrentMeterActivation();
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
    public Response getValidationFeatureStatus(@PathParam("mRID") String mrid) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);

        DeviceValidationStatusInfo deviceValidationStatusInfo = determineStatus(device);
        return Response.status(Response.Status.OK).entity(deviceValidationStatusInfo).build();
    }

    @Path("/validationmonitoring")
    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION,Privileges.VIEW_VALIDATION_CONFIGURATION,com.elster.jupiter.validation.security.Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response getValidationMonitoring(@PathParam("mRID") String mrid) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        ZonedDateTime end = ZonedDateTime.ofInstant(clock.instant(), clock.getZone()).truncatedTo(ChronoUnit.DAYS).plusDays(1);

        ZonedDateTime intervalStart = end.minusYears(1);
        Range<Instant> interval = Range.openClosed(intervalStart.toInstant(), end.toInstant());

        List<DataValidationStatus> statuses = device.getLoadProfiles().stream()
                .flatMap(l -> l.getChannels().stream())
                .flatMap(c -> c.getDevice().forValidation().getValidationStatus(c, Collections.emptyList(), interval).stream())
                .collect(Collectors.toList());

        statuses.addAll(device.getRegisters().stream()
                .flatMap(r -> device.forValidation().getValidationStatus(r, Collections.emptyList(), interval).stream())
                .collect(Collectors.toList()));

        MonitorValidationInfo info = new MonitorValidationInfo(true, statuses, Optional.of(Instant.now()));

        return Response.status(Response.Status.OK).entity(info).build();
    }

    private DeviceValidationStatusInfo determineStatus(Device device) {
        Meter meter = getMeterFor(device);
        DeviceValidation deviceValidation = device.forValidation();
        DeviceValidationStatusInfo deviceValidationStatusInfo =
                new DeviceValidationStatusInfo(
                        deviceValidation.isValidationActive(),
                        deviceValidation.getLastChecked(),
                        meter.hasData());

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
    public Response setValidationFeatureStatus(@PathParam("mRID") String mrid, DeviceValidationStatusInfo deviceValidationStatusInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        Meter meter = getMeterFor(device);
        if (deviceValidationStatusInfo.isActive) {
            validationService.activateValidation(meter);
			validationService.enableValidationOnStorage(meter);
        } else {
            validationService.deactivateValidation(meter);
        }

        if (deviceValidationStatusInfo.isActive && meter.hasData()) {
            if (deviceValidationStatusInfo.lastChecked == null) {
                throw new LocalizedFieldValidationException(MessageSeeds.NULL_DATE, "lastChecked");
            }
            Instant lastCheckedDate = Instant.ofEpochMilli(deviceValidationStatusInfo.lastChecked);
            for (MeterActivation meterActivation : resourceHelper.getMeterActivationsMostCurrentFirst(meter)) {
                if (meterActivation.isEffectiveAt(lastCheckedDate) || meterActivation.getInterval().startsAfter(lastCheckedDate)) {
                    Optional<Instant> meterActivationLastChecked = validationService.getLastChecked(meterActivation);
                    if (meterActivation.isCurrent()) {
                        if (meterActivationLastChecked.isPresent() && lastCheckedDate.isAfter(meterActivationLastChecked.get())) {
                            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_DATE, "lastChecked", meterActivationLastChecked.get());
                        }
                        validationService.updateLastChecked(meterActivation, lastCheckedDate);
                    } else {
                        Instant lastCheckedDateToSet = smallest(meterActivationLastChecked.orElse(meterActivation.getStart()), lastCheckedDate);
                        validationService.updateLastChecked(meterActivation, lastCheckedDateToSet);
                    }
                }
            }
        }

        return Response.status(Response.Status.OK).build();
    }

    private Instant smallest(Instant instant1, Instant instant2) {
        return Ordering.natural().min(instant1, instant2);
    }

    @Path("/validate")
    @PUT
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(com.elster.jupiter.validation.security.Privileges.VALIDATE_MANUAL)
    public Response validateDeviceData(@PathParam("mRID") String mrid) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        Meter meter = getMeterFor(device);
        List<MeterActivation> meterActivations = resourceHelper.getMeterActivationsMostCurrentFirst(meter);
        if (!meterActivations.isEmpty()) {
            Range<Instant> range = meterActivations.get(meterActivations.size() - 1).getRange();
            ValidationEvaluator evaluator = validationService.getEvaluator(meter, range);
            meterActivations.forEach(meterActivation -> {
                if (!evaluator.isAllDataValidated(meterActivation)) {
                    Instant instant = validationService.getLastChecked(meterActivation).orElse(meterActivation.getStart());
                    validationService.validate(meterActivation);
                }
            });
        }
        return Response.status(Response.Status.OK).build();
    }


    private ValidationRuleSet getValidationRuleSet(long validationRuleSetId) {
        Optional<? extends ValidationRuleSet> rulesetRef = validationService.getValidationRuleSet(validationRuleSetId);
        return rulesetRef.orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }


    private Meter getMeterFor(Device device) {
        AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
        Meter meter;
        Optional<Meter> meterRef = amrSystem.findMeter(String.valueOf(device.getId()));
        if (meterRef.isPresent()) {
            meter = meterRef.get();
        } else {
            meter = amrSystem.newMeter(String.valueOf(device.getId()), device.getmRID());
            meter.setSerialNumber(device.getSerialNumber());
            meter.save();
        }
        return meter;
    }
}
