package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.DataValidationStatus;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Reading;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.security.Privileges;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Predicates.not;

public class RegisterDataResource {

    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final Clock clock;

    @Inject
    public RegisterDataResource(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory, Clock clock) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.clock = clock;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_DEVICE)
    public PagedInfoList getRegisterData(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, @BeanParam QueryParameters queryParameters, @Context UriInfo uriInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        Register<?> register = resourceHelper.findRegisterOrThrowException(device, registerId);
        Meter meter = resourceHelper.getMeterFor(device);
        Interval interval = Interval.sinceEpoch();
        List<? extends Reading> readings = register.getReadings(interval);
        List<ReadingRecord> readingRecords = readings.stream().map(Reading::getActualReading).collect(Collectors.toList());
        Optional<Channel> channelRef = resourceHelper.getRegisterChannel(register, meter);
        List<DataValidationStatus> dataValidationStatuses = new ArrayList<>();
        Boolean validationStatusForRegister = false;
        if(channelRef.isPresent()) {
            validationStatusForRegister = device.forValidation().isValidationActive(register, clock.now());
            dataValidationStatuses = device.forValidation().getValidationStatus(register, readingRecords, interval);
        }
        List<ReadingInfo> readingInfos = ReadingInfoFactory.asInfoList(readings, register.getRegisterSpec(),
                validationStatusForRegister, dataValidationStatuses);
        readingInfos = filter(readingInfos, uriInfo.getQueryParameters());

        List<ReadingInfo> paginatedReadingInfo = ListPager.of(readingInfos, ((ri1, ri2) -> ri1.timeStamp.compareTo(ri2.timeStamp))).from(queryParameters).find();
        return PagedInfoList.asJson("data", paginatedReadingInfo, queryParameters);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE)
    public Response editRegisterData(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, @BeanParam QueryParameters queryParameters, ReadingInfo readingInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        Register<?> register = resourceHelper.findRegisterOrThrowException(device, registerId);
        Meter meter = resourceHelper.getMeterFor(device);
        Optional<Channel> channel =  resourceHelper.getRegisterChannel(register, meter);
        if(!channel.isPresent()) {
            exceptionFactory.newException(MessageSeeds.NO_CHANNELS_ON_REGISTER, registerId);
        }

        List<BaseReading> readings = new ArrayList<>();
        readings.add(readingInfo.createNew(register));
        channel.get().editReadings(readings);

        return Response.status(Response.Status.OK).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE)
    public Response addRegisterData(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, @BeanParam QueryParameters queryParameters, ReadingInfo readingInfo) {
        return editRegisterData(mRID, registerId, queryParameters, readingInfo);
    }

    @DELETE
    @Path("/{timeStamp}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE)
    public Response deleteRegisterData(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, @PathParam("timeStamp") long timeStamp, @BeanParam QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        Register<?> register = resourceHelper.findRegisterOrThrowException(device, registerId);
        Meter meter = resourceHelper.getMeterFor(device);
        Optional<Channel> channel =  resourceHelper.getRegisterChannel(register, meter);
        if(!channel.isPresent()) {
            exceptionFactory.newException(MessageSeeds.NO_CHANNELS_ON_REGISTER, registerId);
        }

        List<BaseReadingRecord> readings = channel.get().getReadings(new Interval(new Date(timeStamp), new Date(timeStamp)));
        if(readings.isEmpty()) {
            exceptionFactory.newException(MessageSeeds.NO_SUCH_READING_ON_REGISTER, registerId, timeStamp);
        }
        channel.get().removeReadings(readings);

        return Response.status(Response.Status.OK).build();
    }

    private boolean hasSuspects(ReadingInfo info) {
        boolean result = true;
        if(info instanceof BillingReadingInfo) {
            BillingReadingInfo billingReadingInfo = (BillingReadingInfo)info;
            result = ValidationStatus.SUSPECT.equals(billingReadingInfo.validationResult);
        } else if (info instanceof NumericalReadingInfo) {
            NumericalReadingInfo numericalReadingInfo = (NumericalReadingInfo)info;
            result = ValidationStatus.SUSPECT.equals(numericalReadingInfo.validationResult);
        }
        return result;
    }

    private boolean hideSuspects(ReadingInfo info) {
        return !hasSuspects(info);
    }

    private List<ReadingInfo> filter(List<ReadingInfo> infos, MultivaluedMap<String, String> queryParameters) {
        Predicate<ReadingInfo> fromParams = getFilter(queryParameters);
        return infos.stream().filter(fromParams).collect(Collectors.toList());
    }

    private Predicate<ReadingInfo> getFilter(MultivaluedMap<String, String> queryParameters) {
        ImmutableList.Builder<Predicate<ReadingInfo>> list = ImmutableList.builder();
        boolean onlySuspect = filterActive(queryParameters, "onlySuspect");
        boolean onlyNonSuspect = filterActive(queryParameters, "onlyNonSuspect");
        if (onlySuspect ^ onlyNonSuspect) {
            if (onlySuspect) {
                list.add(this::hasSuspects);
            } else {
                list.add(not(this::hasSuspects));
            }
        }
        if (filterActive(queryParameters, "hideSuspects")) {
            list.add(this::hideSuspects);
        }
        return lpi -> list.build().stream().allMatch(p -> p.test(lpi));
    }

    private boolean filterActive(MultivaluedMap<String, String> queryParameters, String key) {
        return queryParameters.containsKey(key) && Boolean.parseBoolean(queryParameters.getFirst(key));
    }
}
