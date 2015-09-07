package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.util.time.DefaultDateTimeFormatters;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Reading;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.exceptions.NoMeterActivationAt;
import com.energyict.mdc.device.data.rest.DeviceStatesRestricted;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.DateFormat;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Predicates.not;

@DeviceStatesRestricted(
        value = {DefaultState.DECOMMISSIONED},
        methods = {HttpMethod.PUT, HttpMethod.POST, HttpMethod.DELETE},
        ignoredUserRoles = {Privileges.Constants.ADMINISTER_DECOMMISSIONED_DEVICE_DATA})
public class RegisterDataResource {

    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final Clock clock;
    private final DeviceDataInfoFactory deviceDataInfoFactory;

    @Inject
    public RegisterDataResource(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory, Clock clock, DeviceDataInfoFactory deviceDataInfoFactory) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.clock = clock;
        this.deviceDataInfoFactory = deviceDataInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public PagedInfoList getRegisterData(
            @PathParam("mRID") String mRID,
            @PathParam("registerId") long registerId,
            @BeanParam JsonQueryFilter filter,
            @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        Register<?> register = resourceHelper.findRegisterOrThrowException(device, registerId);

        Range<Instant> intervalReg = Range.openClosed(filter.getInstant("intervalStart"), filter.getInstant("intervalEnd"));

        List<? extends Reading> readings = register.getReadings(Interval.of(intervalReg));
        List<ReadingInfo> readingInfos =
                deviceDataInfoFactory.asReadingsInfoList(
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
        readingInfos = filter(readingInfos, filter);
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
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public ReadingInfo getRegisterData(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, @PathParam("timeStamp") long timeStamp) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        Register<?> register = resourceHelper.findRegisterOrThrowException(device, registerId);
        Reading reading = register.getReading(Instant.ofEpochMilli(timeStamp)).orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_READING_ON_REGISTER, registerId, timeStamp));
        return deviceDataInfoFactory.createReadingInfo(
                reading,
                register.getRegisterSpec(),
                device.forValidation().isValidationActive(register, this.clock.instant()));
    }

    @PUT
    @Path("/{timeStamp}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_DATA, Privileges.Constants.ADMINISTER_DECOMMISSIONED_DEVICE_DATA})
    public Response editRegisterData(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, ReadingInfo readingInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        Register<?> register = resourceHelper.findRegisterOrThrowException(device, registerId);
        if((readingInfo instanceof NumericalReadingInfo && NumericalReadingInfo.class.cast(readingInfo).isConfirmed != null && NumericalReadingInfo.class.cast(readingInfo).isConfirmed) ||
                (readingInfo instanceof BillingReadingInfo && BillingReadingInfo.class.cast(readingInfo).isConfirmed != null && BillingReadingInfo.class.cast(readingInfo).isConfirmed)) {
            register.startEditingData().confirmReading(readingInfo.createNew(register)).complete();
        } else {
            register.startEditingData().editReading(readingInfo.createNew(register)).complete();
        }
        return Response.status(Response.Status.OK).build();
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_DATA, Privileges.Constants.ADMINISTER_DECOMMISSIONED_DEVICE_DATA})
    public Response addRegisterData(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, ReadingInfo readingInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        Register<?> register = resourceHelper.findRegisterOrThrowException(device, registerId);
        try {
            register.startEditingData().editReading(readingInfo.createNew(register)).complete();
        } catch (NoMeterActivationAt e) {
            Instant time = (Instant) e.get("time");
            throw this.exceptionFactory.newExceptionSupplier(MessageSeeds.CANT_ADD_READINGS_FOR_STATE, Date.from(time)).get();
        }
        return Response.status(Response.Status.OK).build();
    }

    @DELETE
    @Path("/{timeStamp}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_DATA, Privileges.Constants.ADMINISTER_DECOMMISSIONED_DEVICE_DATA})
    public Response deleteRegisterData(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, @PathParam("timeStamp") long timeStamp, @BeanParam JsonQueryParameters queryParameters) {
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

    private List<ReadingInfo> filter(List<ReadingInfo> infos, JsonQueryFilter filter) {
        Predicate<ReadingInfo> fromParams = getFilter(filter);
        return infos.stream().filter(fromParams).collect(Collectors.toList());
    }

    private Predicate<ReadingInfo> getFilter(JsonQueryFilter filter) {
        ImmutableList.Builder<Predicate<ReadingInfo>> list = ImmutableList.builder();
        if (filter.hasProperty("suspect")){
            List<String> suspectFilters = filter.getStringList("suspect");
            if (suspectFilters.size() == 0) {
                if ("suspect".equals(filter.getString("suspect"))) {
                    list.add(this::hasSuspects);
                } else {
                    list.add(not(this::hasSuspects));
                }
            }
        }
        if (filterActive(filter, "hideSuspects")) {
            list.add(this::hideSuspects);
        }
        return lpi -> list.build().stream().allMatch(p -> p.test(lpi));
    }

    private boolean filterActive(JsonQueryFilter filter, String key) {
        return filter.hasProperty(key) && filter.getBoolean(key);
    }
}
