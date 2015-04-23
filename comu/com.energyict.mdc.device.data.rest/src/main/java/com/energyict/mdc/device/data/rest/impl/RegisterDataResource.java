package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Reading;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.security.Privileges;

import com.elster.jupiter.util.time.Interval;
import com.google.common.collect.ImmutableList;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
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
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    public PagedInfoList getRegisterData(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, @BeanParam QueryParameters queryParameters, @Context UriInfo uriInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        Register<?> register = resourceHelper.findRegisterOrThrowException(device, registerId);
        Interval interval = Interval.sinceEpoch();
        List<? extends Reading> readings = register.getReadings(interval);
        List<ReadingInfo> readingInfos =
                ReadingInfoFactory.asInfoList(
                        readings,
                        register.getRegisterSpec(),
                        device.forValidation().isValidationActive(register, this.clock.instant()));
        // sort the list of readings
        Collections.sort(readingInfos, (ri1, ri2) -> ri2.timeStamp.compareTo(ri1.timeStamp));
        /* And fill a delta value for cumulative reading type. The delta is the difference with the previous record.
           The Delta value won't be stored in the database yet, as it has a performance impact */
        if (register.getReadingType().isCumulative()){
            for (int i = 0; i < readingInfos.size() - 1; i++){
                calculateDeltaForNumericalReading(readingInfos.get(i + 1), readingInfos.get(i));
                calculateDeltaForBillingReading(readingInfos.get(i + 1), readingInfos.get(i));
            }
        }
        // filter the list of readings based on user parameters
        readingInfos = filter(readingInfos, uriInfo.getQueryParameters());
        List<ReadingInfo> paginatedReadingInfo = ListPager.of(readingInfos).from(queryParameters).find();
        return PagedInfoList.fromPagedList("data", paginatedReadingInfo, queryParameters);
    }

    private void calculateDeltaForNumericalReading(ReadingInfo previous, ReadingInfo current){
        if (previous != null && current != null && current instanceof NumericalReadingInfo){
            NumericalReadingInfo prevCasted = (NumericalReadingInfo) previous;
            NumericalReadingInfo curCasted = (NumericalReadingInfo) current;
            if (prevCasted.value != null && curCasted.value != null) {
                curCasted.deltaValue = curCasted.value.subtract(prevCasted.value);
                curCasted.deltaValue = curCasted.deltaValue.setScale(curCasted.value.scale(), BigDecimal.ROUND_UP);
            }
        }
    }

    private void calculateDeltaForBillingReading(ReadingInfo previous, ReadingInfo current){
        if (previous != null && current != null && current instanceof BillingReadingInfo){
            BillingReadingInfo prevCasted = (BillingReadingInfo) previous;
            BillingReadingInfo curCasted = (BillingReadingInfo) current;
            if (prevCasted.value != null && curCasted.value != null) {
                curCasted.deltaValue = curCasted.value.subtract(prevCasted.value);
            }
        }
    }
    @GET
    @Path("/{timeStamp}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    public ReadingInfo getRegisterData(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, @PathParam("timeStamp") long timeStamp) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        Register<?> register = resourceHelper.findRegisterOrThrowException(device, registerId);
        Reading reading = register.getReading(Instant.ofEpochMilli(timeStamp)).orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_READING_ON_REGISTER, registerId, timeStamp));
        return ReadingInfoFactory.asInfo(
                reading,
                register.getRegisterSpec(),
                device.forValidation().isValidationActive(register, this.clock.instant()));
    }

    @PUT
    @Path("/{timeStamp}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_DATA)
    public Response editRegisterData(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, ReadingInfo readingInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        Register<?> register = resourceHelper.findRegisterOrThrowException(device, registerId);
        register.startEditingData().editReading(readingInfo.createNew(register)).complete();
        return Response.status(Response.Status.OK).build();
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_DATA)
    public Response addRegisterData(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, ReadingInfo readingInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        Register<?> register = resourceHelper.findRegisterOrThrowException(device, registerId);
        register.startEditingData().editReading(readingInfo.createNew(register)).complete();
        return Response.status(Response.Status.OK).build();
    }

    @DELETE
    @Path("/{timeStamp}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_DATA)
    public Response deleteRegisterData(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, @PathParam("timeStamp") long timeStamp, @BeanParam QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        Register<?> register = resourceHelper.findRegisterOrThrowException(device, registerId);
        try {
            register.startEditingData().removeReading(Instant.ofEpochMilli(timeStamp)).complete();
        }
        catch (IllegalArgumentException e) {
            throw this.exceptionFactory.newExceptionSupplier(MessageSeeds.NO_CHANNELS_ON_REGISTER, registerId).get();
        }
        return Response.status(Response.Status.OK).build();
    }

    private boolean hasSuspects(ReadingInfo info) {
        boolean result = true;
        if (info instanceof BillingReadingInfo) {
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
