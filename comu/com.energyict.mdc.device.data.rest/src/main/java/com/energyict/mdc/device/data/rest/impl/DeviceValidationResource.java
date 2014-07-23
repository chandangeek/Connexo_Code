package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.*;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.*;
import com.elster.jupiter.validation.rest.ValidationRuleSetInfo;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

public class DeviceValidationResource {
    private final ResourceHelper resourceHelper;
    private final ValidationService validationService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceDataService deviceDataService;
    private final MeteringService meteringService;
    private final ExceptionFactory exceptionFactory;
/*
    private static boolean ACTIVE = true;
    private static boolean INACTIVE = false;*/

    @Inject
    public DeviceValidationResource (ResourceHelper resourceHelper, ValidationService validationService, DeviceConfigurationService deviceConfigurationService, DeviceDataService deviceDataService, MeteringService meteringService, ExceptionFactory exceptionFactory) {
        this.resourceHelper = resourceHelper;
        this.validationService = validationService;
        this.deviceDataService = deviceDataService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.meteringService = meteringService;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getValidationRulsetsForDevice(@PathParam("mRID") String mrid, @BeanParam QueryParameters queryParameters) {
        List<DeviceValidationRuleSetInfo> result = new ArrayList<>();
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        MeterActivation activation = getCurrentMeterActivation(device);

        DeviceConfiguration deviceConfig = deviceConfigurationService.findDeviceConfiguration(device.getDeviceConfiguration().getId());
        if (deviceConfig != null) {
            List<ValidationRuleSet> linkedRuleSets = deviceConfig.getValidationRuleSets();
            fillValidationRuleSetStatus(linkedRuleSets, activation, result);
        }
        Collections.sort(result, ValidationRuleSetInfo.VALIDATION_RULESET_NAME_COMPARATOR);
        return Response.ok(PagedInfoList.asJson("rulesets",
                ListPager.of(result).from(queryParameters).find(), queryParameters)).build();
    }

    private void fillValidationRuleSetStatus(List<ValidationRuleSet> linkedRuleSets, MeterActivation activation, List<DeviceValidationRuleSetInfo> result) {
        List<MeterActivationValidation> validations = validationService.getMeterActivationValidationsForMeterActivation(activation);
        for(ValidationRuleSet ruleset : linkedRuleSets) {
            for (MeterActivationValidation validation : validations) {
                if(validation.getRuleSet().equals(ruleset)) {
                    result.add(new DeviceValidationRuleSetInfo(ruleset, validation.isActive()));
                }
            }
        }
    }

    @Path("/{validationRuleSetId}/status")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response setValidationRuleSetStatusOnDevice(@PathParam("mRID") String mrid, @PathParam("validationRuleSetId") long validationRuleSetId, boolean status) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        ValidationRuleSet ruleset = getValidationRuleSet(validationRuleSetId);
        MeterActivation activation = getCurrentMeterActivation(device);
        setValidationRuleSetActivationStatus(activation, ruleset, status);
        return Response.status(Response.Status.OK).build();
    }

    private void setValidationRuleSetActivationStatus(MeterActivation activation, ValidationRuleSet ruleset, boolean status) {
        List<MeterActivationValidation> validations = validationService.getMeterActivationValidationsForMeterActivation(activation);
        for(MeterActivationValidation validation : validations) {
            if(validation.getRuleSet().equals(ruleset)) {
                validation.setActive(status);
                validation.save();
            }
        }
    }

    @Path("/validationstatus")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getValidationFeatureStatus(@PathParam("mRID") String mrid) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        MeterActivation activation = getCurrentMeterActivation(device);
        Optional<MeterValidation> meterValidationRef = validationService.getMeterValidation(activation);
        if(!meterValidationRef.isPresent()) {
            meterValidationRef = Optional.of(validationService.createMeterValidation(activation));
        }
        Date minDate = validationService.getLastChecked(activation);
        return Response.status(Response.Status.OK)
                .entity(new DeviceValidationStatusInfo(meterValidationRef.get().getActivationStatus(), minDate)).build();
    }

    @Path("/validationstatus")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response deactivateValidationFeatureOnDevice(@PathParam("mRID") String mrid, boolean status) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        MeterActivation activation = getCurrentMeterActivation(device);
        validationService.setMeterValidationStatus(activation, status);
        return Response.status(Response.Status.OK).build();
    }

    @Path("/validate")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response activateValidationFeatureOnDevice(@PathParam("mRID") String mrid, Date date) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        MeterActivation activation = getCurrentMeterActivation(device);
        Date maxDate = validationService.getLastChecked(activation);
        if(date == null || (date != null && date.after(maxDate))) {
            throw exceptionFactory.newException(MessageSeeds.INVALID_DATE, maxDate);
        }
        validationService.setLastChecked(activation, date);
        validationService.validate(activation, Interval.startAt(date));
        return Response.status(Response.Status.OK).build();
    }

    private ValidationRuleSet getValidationRuleSet(long validationRuleSetId) {
        Optional<ValidationRuleSet> rulesetRef = validationService.getValidationRuleSet(validationRuleSetId);
        if(!rulesetRef.isPresent()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return rulesetRef.get();
    }

    private MeterActivation getCurrentMeterActivation(Device device) {
        Optional<AmrSystem> amrSystemRef = meteringService.findAmrSystem(1);
        Optional<Meter> meterRef = amrSystemRef.get().findMeter(String.valueOf(device.getId()));
        if(!meterRef.isPresent()) {
            Meter meter = amrSystemRef.get().newMeter(String.valueOf(device.getId()), device.getmRID());
            meter.save();
            meterRef = Optional.of(meter);
        }

        Optional<MeterActivation> activationRef = meterRef.get().getCurrentMeterActivation();
        if(!activationRef.isPresent()) {
            Date date = new Date();
            MeterActivation meterActivation = meterRef.get().activate(date);
            activationRef = Optional.of(meterActivation);
            validationService.createMeterValidation(meterActivation);
            validationService.getMeterActivationValidations(meterActivation, Interval.startAt(date));
        }
        return activationRef.get();
    }
}
