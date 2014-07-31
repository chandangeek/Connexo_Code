package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.ChannelValidation;
import com.elster.jupiter.validation.MeterActivationValidation;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
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
import com.google.common.util.concurrent.Striped;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

public class DeviceValidationResource {
    private final ResourceHelper resourceHelper;
    private final ValidationService validationService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceDataService deviceDataService;
    private final MeteringService meteringService;
    private final ExceptionFactory exceptionFactory;
    private final Clock clock;
    private final Striped<ReadWriteLock> stripedLock = Striped.readWriteLock(32);

    @Inject
    public DeviceValidationResource (ResourceHelper resourceHelper, ValidationService validationService, DeviceConfigurationService deviceConfigurationService, DeviceDataService deviceDataService, MeteringService meteringService, ExceptionFactory exceptionFactory, Clock clock) {
        this.resourceHelper = resourceHelper;
        this.validationService = validationService;
        this.deviceDataService = deviceDataService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.meteringService = meteringService;
        this.exceptionFactory = exceptionFactory;
        this.clock = clock;
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
        List<? extends MeterActivationValidation> validations = validationService.getMeterActivationValidationsForMeterActivation(activation);
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
        List<? extends MeterActivationValidation> validations = validationService.getMeterActivationValidationsForMeterActivation(activation);
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
        Date minDate = validationService.getLastChecked(activation);
        DeviceValidationStatusInfo deviceValidationStatusInfo = new DeviceValidationStatusInfo
                (validationService.getMeterValidation(activation).get().getActivationStatus(), minDate);
        return Response.status(Response.Status.OK)
                .entity(deviceValidationStatusInfo).build();
    }

    @Path("/validationstatus")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response setValidationFeatureStatus(@PathParam("mRID") String mrid, boolean status) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        MeterActivation activation = getCurrentMeterActivation(device);
        validationService.setMeterValidationStatus(activation, status);
        return Response.status(Response.Status.OK).build();
    }

    @Path("/validate")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response ValidateDeviceData(@PathParam("mRID") String mrid, Date date) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        MeterActivation activation = getCurrentMeterActivation(device);
        Date maxDate = validationService.getLastChecked(activation);
        if(date == null || date.after(maxDate)) {
            throw exceptionFactory.newException(MessageSeeds.INVALID_DATE, maxDate);
        }
        validationService.setLastChecked(activation, date);
        validationService.validate(activation, Interval.startAt(date));
        return Response.status(Response.Status.OK).build();
    }

    @Path("/devicevalidation")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response activateValidationFeatureOnDevice(@PathParam("mRID") String mrid) {
        Lock lock = stripedLock.get(mrid).writeLock(); // we use striped locking to improve concurrency.
        lock.lock();
        try {
            Date now =  clock.now();
            Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
            MeterActivation meterActivation = findOrCreateMeterActivation(now, findOrCreateMeter(device));
            if(!validationService.getMeterValidation(meterActivation).isPresent()) {
                validationService.createMeterValidation(meterActivation);
            }
            return Response.ok(determineStatus(meterActivation, now)).build();
        } finally {
            lock.unlock();
        }
    }

    private MeterActivation findOrCreateMeterActivation(Date now, Meter meter) {
        MeterActivation meterActivation;
        Optional<MeterActivation> activationRef = meter.getCurrentMeterActivation();
        if(activationRef.isPresent()) {
            meterActivation = activationRef.get();
        } else {
            meterActivation = meter.activate(now);
        }
        return meterActivation;
    }

    private Meter findOrCreateMeter(Device device) {
        AmrSystem amrSystem = meteringService.findAmrSystem(1).get();
        Meter meter;
        Optional<Meter> meterRef = amrSystem.findMeter(String.valueOf(device.getId()));
        if(meterRef.isPresent()) {
            meter = meterRef.get();
        } else {
            meter = amrSystem.newMeter(String.valueOf(device.getId()), device.getmRID());
            meter.save();
        }
        return meter;
    }

    private DeviceValidationStatusInfo determineStatus(MeterActivation meterActivation, Date now) {
        DeviceValidationStatusInfo status = new DeviceValidationStatusInfo();
        for (MeterActivationValidation meterActivationValidation : findOrCreateActivationValidation(meterActivation, now)) {
            apply(status, meterActivationValidation);
        }
        return status;
    }

    private void apply(DeviceValidationStatusInfo status, MeterActivationValidation meterActivationValidation) {
        status.isActive = status.isActive || meterActivationValidation.isActive();
        for (ChannelValidation channelValidation : meterActivationValidation.getChannelValidations()) {
            status.hasValidation = status.hasValidation || channelValidation.getLastChecked() != null;
            if (status.hasValidation) {
                status.lastChecked = max(status.lastChecked, channelValidation.getLastChecked());
            }
        }
    }

    private List<? extends MeterActivationValidation> findOrCreateActivationValidation(MeterActivation meterActivation, Date now) {
        List<? extends MeterActivationValidation> meterActivationValidations = validationService.getMeterActivationValidationsForMeterActivation(meterActivation);
        if (meterActivationValidations.isEmpty()) {
            meterActivationValidations = validationService.getMeterActivationValidations(meterActivation, Interval.startAt(now));
        }
        return meterActivationValidations;
    }

    private Date max(Date date1, Date date2) {
        return NullSafe.DATE_COMPARATOR.compare(date1, date2) >= 0 ? date1 : date2;
    }

    private static enum NullSafe implements Comparator<Date> {
        DATE_COMPARATOR {
            @Override
            public int compare(Date o1, Date o2) {
                if (o1 == null) {
                    return o2 == null ? 0 : -1;
                }
                if (o2 == null) {
                    return 1;
                }
                return o1.compareTo(o2);
            }
        }
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
            throw new IllegalArgumentException("Validation feature on device " + device.getmRID()  +
                    " wasn't initialized properly. Please request feature initialization using GET request on " +
                    "\"api/ddr/devices/{MRID}/validationrulesets/devicevalidation\" first of all.") ;
        }

        Optional<MeterActivation> activationRef = meterRef.get().getCurrentMeterActivation();
        if(!activationRef.isPresent()) {
            throw new IllegalArgumentException("Validation feature on device " + device.getmRID()  +
                    " wasn't initialized properly. Please request feature initialization using GET request on " +
                    "\"api/ddr/devices/{MRID}/validationrulesets/devicevalidation\" first of all.") ;
        }
        return activationRef.get();
    }
}
