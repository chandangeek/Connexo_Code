package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.MeterActivationValidation;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.ValidationRuleSetInfo;
import com.elster.jupiter.validation.security.Privileges;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceValidation;
import com.google.common.base.Optional;
import com.google.common.collect.Ordering;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
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
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_VALIDATION_CONFIGURATION)
    public Response getValidationRulsetsForDevice(@PathParam("mRID") String mrid, @BeanParam QueryParameters queryParameters) {
        List<DeviceValidationRuleSetInfo> result = new ArrayList<>();
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        Meter meter = getMeterFor(device);

        Optional<MeterActivation> activation = meter.getCurrentMeterActivation();

        DeviceConfiguration deviceConfig = deviceConfigurationService.findDeviceConfiguration(device.getDeviceConfiguration().getId());
        if (deviceConfig != null) {
            List<ValidationRuleSet> linkedRuleSets = deviceConfig.getValidationRuleSets();
            fillValidationRuleSetStatus(linkedRuleSets, activation, result);
        }
        Collections.sort(result, ValidationRuleSetInfo.VALIDATION_RULESET_NAME_COMPARATOR);
        return Response.ok(PagedInfoList.asJson("rulesets",
                ListPager.of(result).from(queryParameters).find(), queryParameters)).build();
    }

    private void fillValidationRuleSetStatus(List<ValidationRuleSet> linkedRuleSets, Optional<MeterActivation> activation, List<DeviceValidationRuleSetInfo> result) {
        List<? extends MeterActivationValidation> validations = activation.isPresent() ? validationService.getMeterActivationValidations(activation.get()) : Collections.<MeterActivationValidation>emptyList();
        for (ValidationRuleSet ruleset : linkedRuleSets) {
            MeterActivationValidation meterActivationValidationForRuleset = getMeterActivationValidationForRuleset(validations, ruleset);
            result.add(new DeviceValidationRuleSetInfo(ruleset, meterActivationValidationForRuleset == null || meterActivationValidationForRuleset.isActive()));
        }
    }

    private MeterActivationValidation getMeterActivationValidationForRuleset(List<? extends MeterActivationValidation> validations, ValidationRuleSet ruleset) {
        for (MeterActivationValidation validation : validations) {
            if (validation.getRuleSet().equals(ruleset)) {
                return validation;
            }
        }
        return null;
    }

    @Path("/{validationRuleSetId}/status")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_VALIDATION_CONFIGURATION)
    public Response setValidationRuleSetStatusOnDevice(@PathParam("mRID") String mrid, @PathParam("validationRuleSetId") long validationRuleSetId, boolean status) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        Meter meter = getMeterFor(device);
        ValidationRuleSet ruleSet = getValidationRuleSet(validationRuleSetId);
        Optional<MeterActivation> activation = meter.getCurrentMeterActivation();
        if (activation.isPresent()) {
            setValidationRuleSetActivationStatus(activation.get(), ruleSet, status);
        } else {
            throw exceptionFactory.newException(MessageSeeds.DEACTIVATE_VALIDATION_RULE_SET_NOT_POSSIBLE, ruleSet.getName());
        }
        return Response.status(Response.Status.OK).build();
    }

    private void setValidationRuleSetActivationStatus(MeterActivation activation, ValidationRuleSet ruleset, boolean status) {
        List<? extends MeterActivationValidation> validations = validationService.getMeterActivationValidations(activation);
        for (MeterActivationValidation validation : validations) {
            if (validation.getRuleSet().equals(ruleset)) {
                if (status) {
                    validation.activate();
                } else {
                    validation.deactivate();
                }
                validation.save();
            }
        }
    }

    @Path("/validationstatus")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_VALIDATION_CONFIGURATION)
    public Response getValidationFeatureStatus(@PathParam("mRID") String mrid) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);

        DeviceValidationStatusInfo deviceValidationStatusInfo = determineStatus(device);
        return Response.status(Response.Status.OK).entity(deviceValidationStatusInfo).build();
    }

    private DeviceValidationStatusInfo determineStatus(Device device) {
        Meter meter = getMeterFor(device);
        DeviceValidation deviceValidation = device.forValidation();
        DeviceValidationStatusInfo deviceValidationStatusInfo = new DeviceValidationStatusInfo(deviceValidation.isValidationActive(), deviceValidation.getLastChecked().or(clock.now()), meter.hasData());

        ZonedDateTime end = ZonedDateTime.ofInstant(clock.now().toInstant(), clock.getTimeZone().toZoneId()).truncatedTo(ChronoUnit.DAYS).plusDays(1);

        collectRegisterData(device, deviceValidationStatusInfo, end);
        collectLoadProfileData(device, deviceValidationStatusInfo, end);

        return deviceValidationStatusInfo;
    }

    private void collectLoadProfileData(Device device, DeviceValidationStatusInfo deviceValidationStatusInfo, ZonedDateTime end) {
        ZonedDateTime loadProfileStart = end.minusMonths(1);
        Interval loadProfileInterval = new Interval(Date.from(loadProfileStart.toInstant()), Date.from(end.toInstant()));

        List<DataValidationStatus> statuses = device.getLoadProfiles().stream()
                .flatMap(l -> l.getChannels().stream())
                .flatMap(c -> c.getDevice().forValidation().getValidationStatus(c, Collections.emptyList(), loadProfileInterval).stream())
                .collect(Collectors.toList());

        deviceValidationStatusInfo.loadProfileSuspectCount = statuses.stream()
                .flatMap(d -> d.getReadingQualities().stream())
                .filter(ValidationService.IS_VALIDATION_QUALITY)
                .count();
        if (statuses.isEmpty()) {
            deviceValidationStatusInfo.allDataValidated &= device.getRegisters().stream()
                    .allMatch(r -> r.getDevice().forValidation().allDataValidated(r, clock.now()));
        } else {
            deviceValidationStatusInfo.allDataValidated &= statuses.stream()
                    .allMatch(DataValidationStatus::completelyValidated);
        }
    }

    private void collectRegisterData(Device device, DeviceValidationStatusInfo deviceValidationStatusInfo, ZonedDateTime end) {
        ZonedDateTime registerStart = end.minusYears(1);
        Interval registerInterval = new Interval(Date.from(registerStart.toInstant()), Date.from(end.toInstant()));

        List<DataValidationStatus> statuses = device.getRegisters().stream()
                .flatMap(r -> device.forValidation().getValidationStatus(r, Collections.emptyList(), registerInterval).stream())
                .collect(Collectors.toList());

        deviceValidationStatusInfo.registerSuspectCount = statuses.stream()
                .flatMap(d -> d.getReadingQualities().stream())
                .filter(ValidationService.IS_VALIDATION_QUALITY)
                .count();
        if (statuses.isEmpty()) {
            deviceValidationStatusInfo.allDataValidated &= device.getLoadProfiles().stream()
                    .flatMap(l -> l.getChannels().stream())
                    .allMatch(r -> r.getDevice().forValidation().allDataValidated(r, clock.now()));
        } else {
            deviceValidationStatusInfo.allDataValidated = statuses.stream()
                    .allMatch(DataValidationStatus::completelyValidated);
        }
    }

    @Path("/validationstatus")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION)
    public Response setValidationFeatureStatus(@PathParam("mRID") String mrid, DeviceValidationStatusInfo deviceValidationStatusInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        Meter meter = getMeterFor(device);
        if (deviceValidationStatusInfo.isActive) {
            validationService.activateValidation(meter);
        } else {
            validationService.deactivateValidation(meter);
        }

        if (deviceValidationStatusInfo.isActive && meter.hasData()) {
            if (deviceValidationStatusInfo.lastChecked == null) {
                throw new LocalizedFieldValidationException(MessageSeeds.NULL_DATE, "lastChecked");
            }
            Date lastCheckedDate = new Date(deviceValidationStatusInfo.lastChecked);
            for (MeterActivation meterActivation : resourceHelper.getMeterActivationsMostCurrentFirst(meter)) {
                if (meterActivation.isEffective(lastCheckedDate) || meterActivation.getInterval().startsAfter(lastCheckedDate)) {
                    Optional<Date> meterActivationLastChecked = validationService.getLastChecked(meterActivation);
                    if (meterActivation.isCurrent()) {
                        if (meterActivationLastChecked.isPresent() && lastCheckedDate.after(meterActivationLastChecked.get())) {
                            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_DATE, "lastChecked", meterActivationLastChecked.get());
                        }
                        validationService.updateLastChecked(meterActivation, lastCheckedDate);
                    } else {
                        Date lastCheckedDateToSet = smallest(meterActivationLastChecked.or(meterActivation.getStart()), lastCheckedDate);
                        validationService.updateLastChecked(meterActivation, lastCheckedDateToSet);
                    }
                }
            }
        }

        return Response.status(Response.Status.OK).build();
    }

    private Date smallest(Date date1, Date date2) {
        return Ordering.natural().min(date1, date2);
    }

    @Path("/validate")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(com.energyict.mdc.device.data.security.Privileges.VALIDATE_DEVICE)
    public Response validateDeviceData(@PathParam("mRID") String mrid) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        Meter meter = getMeterFor(device);
        resourceHelper.getMeterActivationsMostCurrentFirst(meter).forEach(meterActivation -> {
            if (!validationService.getEvaluator().isAllDataValidated(meterActivation)) {
                Date date = validationService.getLastChecked(meterActivation).or(meterActivation.getStart());
                validationService.validate(meterActivation, Interval.startAt(date));
            }
        });
        return Response.status(Response.Status.OK).build();
    }


    private ValidationRuleSet getValidationRuleSet(long validationRuleSetId) {
        Optional<ValidationRuleSet> rulesetRef = validationService.getValidationRuleSet(validationRuleSetId);
        if (!rulesetRef.isPresent()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return rulesetRef.get();
    }


    private Meter getMeterFor(Device device) {
        AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
        Meter meter;
        Optional<Meter> meterRef = amrSystem.findMeter(String.valueOf(device.getId()));
        if (meterRef.isPresent()) {
            meter = meterRef.get();
        } else {
            meter = amrSystem.newMeter(String.valueOf(device.getId()), device.getmRID());
            meter.save();
        }
        return meter;
    }
}
