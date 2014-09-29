package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.Reading;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.security.Privileges;
import com.google.common.base.Optional;

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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
    @RolesAllowed(Privileges.VIEW_DEVICE)
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
    @RolesAllowed(Privileges.VIEW_DEVICE)
    public RegisterInfo getRegister(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId) {
        Register<?> register = doGetRegister(mRID, registerId);
        return RegisterInfoFactory.asInfo(register, validationInfoHelper.getRegisterValidationInfo(register));
    }

/*    @Override
    public void validate(MeterActivation meterActivation, String readingTypeCode, Interval interval)*/

    @PUT
    @Path("/{registerId}/validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_DEVICE)
    public Response validateNow(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, Long date) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        Register<?> register = doGetRegister(mRID, registerId);

        Meter meter = resourceHelper.getMeterFor(device);
        Optional<Channel> channelRef = resourceHelper.getRegisterChannel(register, meter);
        if(!channelRef.isPresent()) {
            throw new WebApplicationException("There is no channel for that register");
        }
        if (date == null) {
            throw new LocalizedFieldValidationException(MessageSeeds.NULL_DATE, "lastChecked");
        }
        Date newDate = new Date(date);
        Optional<Date> lastChecked = device.forValidation().getLastChecked(register);
        if (lastChecked.isPresent() && newDate.after(lastChecked.get())) {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_DATE, "lastChecked", lastChecked.get());
        }
        device.forValidation().validateRegister(register, newDate, null);
        return Response.status(Response.Status.OK).build();
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
    @RolesAllowed(com.elster.jupiter.validation.security.Privileges.VIEW_VALIDATION_CONFIGURATION)
    public Response getValidationFeatureStatus(@PathParam("mRID") String mrid, @PathParam("registerId") long registerId) {
        Register<?> register = doGetRegister(mrid, registerId);
        ValidationStatusInfo validationStatusInfo = determineStatus(register);
        return Response.status(Response.Status.OK).entity(validationStatusInfo).build();
    }

    private ValidationStatusInfo determineStatus(Register<?> register) {
        return new ValidationStatusInfo(isValidationActive(register), lastChecked(register), hasData(register));
    }

    private boolean isValidationActive(Register<?> register) {
        return register.getDevice().forValidation().isValidationActive(register, clock.now());
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
        return register.getDevice().forValidation().getLastChecked(register).orNull();
    }

    private List<DataValidationStatus> statuses(Register<?> register) {
        List<? extends Reading> readings = getReadingsForOneYear(register);
        List<ReadingRecord> readingRecords = readings.stream().map(Reading::getActualReading).collect(Collectors.toList());
        return register.getDevice().forValidation().getValidationStatus(register, readingRecords);
    }

    private boolean validationActive(Register<?> register, DeviceValidation deviceValidation) {
        return deviceValidation.isValidationActive(register, clock.now());
    }

    private List<? extends Reading> getReadingsForOneYear(Register<?> register) {
        return register.getReadings(lastYear());
    }

    private Interval lastYear() {
        ZonedDateTime end = clock.now().toInstant().atZone(ZoneId.of("UTC")).with(ChronoField.MILLI_OF_DAY, 0L).plusDays(1);
        ZonedDateTime start = end.minusYears(1);
        return new Interval(Date.from(start.toInstant()), Date.from(end.toInstant()));
    }

}
