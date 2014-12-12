package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.Reading;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.security.Privileges;

import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class RegisterResource {

    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final Provider<RegisterDataResource> registerDataResourceProvider;
    private final ValidationInfoHelper validationInfoHelper;
    private final Clock clock;

    @Inject
    public RegisterResource(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory, Provider<RegisterDataResource> registerDataResourceProvider, ValidationInfoHelper validationInfoHelper, ValidationService validationService, Clock clock) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.registerDataResourceProvider = registerDataResourceProvider;
        this.clock = clock;
        this.validationInfoHelper = validationInfoHelper;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    public PagedInfoList getRegisters(@PathParam("mRID") String mRID, @BeanParam QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        List<Register> registers = ListPager.of(device.getRegisters(),
                (r1, r2) -> r1.getRegisterSpec().getRegisterType().getName().compareToIgnoreCase(r2.getRegisterSpec().getRegisterType().getName()))
                .from(queryParameters).find();

        List<RegisterInfo> registerInfos = RegisterInfoFactory.asInfoList(registers, validationInfoHelper);
        return PagedInfoList.asJson("data", registerInfos, queryParameters);
    }

    @GET
    @Path("/{registerId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    public RegisterInfo getRegister(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId) {
        Register<?> register = doGetRegister(mRID, registerId);
        return RegisterInfoFactory.asInfo(register, validationInfoHelper.getRegisterValidationInfo(register));
    }

    @PUT
    @Path("/{registerId}/validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({com.elster.jupiter.validation.security.Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION, com.elster.jupiter.validation.security.Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response validateNow(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, TriggerValidationInfo validationInfo) {
        Register<?> register = doGetRegister(mRID, registerId);
        if (validationInfo.lastChecked == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.NULL_DATE, "lastChecked");
        }
        Date newDate = new Date(validationInfo.lastChecked);
        Date lastChecked = lastChecked(register);
        if (lastChecked != null && newDate.after(lastChecked)) {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_DATE, "lastChecked", lastChecked);
        }
        validateRegister(register, newDate.toInstant());
        return Response.status(Response.Status.OK).build();
    }

    private void validateRegister(Register<?> register, Instant start) {
    	if (start != null) {
    		register.getDevice().forValidation().setLastChecked(register, start);
    	}
        register.getDevice().forValidation().validateRegister(register);
    }

    private Register<?> doGetRegister(String mRID, long registerId) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        return resourceHelper.findRegisterOrThrowException(device, registerId);
    }

    @Path("/{registerId}/data")
    public RegisterDataResource getRegisterDataResource() {
        return registerDataResourceProvider.get();
    }

    @Path("{registerId}/validationstatus")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({com.elster.jupiter.validation.security.Privileges.ADMINISTRATE_VALIDATION_CONFIGURATION,com.elster.jupiter.validation.security.Privileges.VIEW_VALIDATION_CONFIGURATION,com.elster.jupiter.validation.security.Privileges.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE})
    public Response getValidationFeatureStatus(@PathParam("mRID") String mrid, @PathParam("registerId") long registerId) {
        Register<?> register = doGetRegister(mrid, registerId);
        ValidationStatusInfo validationStatusInfo = determineStatus(register);
        return Response.status(Response.Status.OK).entity(validationStatusInfo).build();
    }

    private ValidationStatusInfo determineStatus(Register<?> register) {
        return new ValidationStatusInfo(isValidationActive(register), lastChecked(register), hasData(register));
    }

    private boolean isValidationActive(Register<?> register) {
        return register.getDevice().forValidation().isValidationActive(register, clock.instant());
    }

    private boolean hasData(Register<?> channel) {
        return channel.getDevice().forValidation().hasData(channel);
    }

    private DetailedValidationInfo getRegisterValidationInfo(Register<?> register) {
        boolean validationActive = validationActive(register, register.getDevice().forValidation());
        Date lastChecked = lastChecked(register);
        return new DetailedValidationInfo(validationActive, statuses(register), lastChecked);
    }

    private Date lastChecked(Register<?> register) {
        return register.getDevice().forValidation().getLastChecked(register)
                .map(Date::from)
                .orElse(null);
    }

    private List<DataValidationStatus> statuses(Register<?> register) {
        List<? extends Reading> readings = getReadingsForOneYear(register);
        List<ReadingRecord> readingRecords = readings.stream().map(Reading::getActualReading).collect(Collectors.toList());
        return register.getDevice().forValidation().getValidationStatus(register, readingRecords);
    }

    private boolean validationActive(Register<?> register, DeviceValidation deviceValidation) {
        return deviceValidation.isValidationActive(register, clock.instant());
    }

    private List<? extends Reading> getReadingsForOneYear(Register<?> register) {
        return register.getReadings(lastYear());
    }

    private Interval lastYear() {
        ZonedDateTime end = clock.instant().atZone(ZoneId.systemDefault()).with(ChronoField.MILLI_OF_DAY, 0L).plusDays(1);
        ZonedDateTime start = end.minusYears(1);
        return new Interval(Date.from(start.toInstant()), Date.from(end.toInstant()));
    }

}