package com.elster.insight.usagepoint.data.rest.impl;

import static com.elster.jupiter.util.streams.Predicates.not;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.elster.insight.common.rest.ExceptionFactory;
import com.elster.insight.common.services.ListPager;
import com.elster.insight.usagepoint.data.UsagePointValidation;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.Ranges;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

public class RegisterDataResource {

    private final ResourceHelper resourceHelper;
    private final RegisterResourceHelper registerHelper;
    private final ExceptionFactory exceptionFactory;
    private final Clock clock;
    private final UsagePointDataInfoFactory usagePointDataInfoFactory;

    @Inject
    public RegisterDataResource(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory, Clock clock, RegisterResourceHelper registerHelper, UsagePointDataInfoFactory usagePointDataInfoFactory) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.clock = clock;
        this.registerHelper = registerHelper;
        this.usagePointDataInfoFactory = usagePointDataInfoFactory;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.BROWSE_ANY, Privileges.Constants.BROWSE_OWN})
    public Response getRegisterData(
            @PathParam("mrid") String mrid,
            @PathParam("rt_mrid") String rt_mrid,
            @BeanParam JsonQueryFilter filter,
            @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByMrIdOrThrowException(mrid);
        ReadingType readingType = resourceHelper.findReadingTypeByMrIdOrThrowException(rt_mrid);
        
        UsagePointValidation upv = registerHelper.getUsagePointValidation(usagePoint);
        boolean validationEnabled = upv.isValidationActive();
        Channel channel = registerHelper.findRegisterOnUsagePoint(mrid, rt_mrid)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_CHANNEL_FOR_USAGE_POINT_FOR_MRID, mrid, rt_mrid));
        
        if (filter.hasProperty("intervalStart") && filter.hasProperty("intervalEnd")) {

            Range<Instant> range = Ranges.openClosed(filter.getInstant("intervalStart"), filter.getInstant("intervalEnd"));
            List<? extends BaseReadingRecord> registerDataInfo = usagePoint.getReadings(range, readingType);
            List<RegisterDataInfo> infos = registerDataInfo.stream().map(
                    readingRecord -> usagePointDataInfoFactory.createRegisterDataInfo(readingRecord, validationEnabled, channel, upv)).collect(Collectors.toList());

            Collections.sort(infos, (ri1, ri2) -> ri2.readingTime.compareTo(ri1.readingTime));
            /* And fill a delta value for cumulative reading type. The delta is the difference with the previous record.
               The Delta value won't be stored in the database yet, as it has a performance impact */
            if (readingType.isCumulative()) {
                for (int i = 0; i < infos.size() - 1; i++) {
                    calculateDeltaForNumericalReading(infos.get(i + 1), infos.get(i));
                }
            }

            infos = filter(infos, filter);
            List<RegisterDataInfo> paginatedChannelData = ListPager.of(infos).from(queryParameters).find();
            PagedInfoList pagedInfoList = PagedInfoList.fromPagedList("data", paginatedChannelData, queryParameters);
            return Response.ok(pagedInfoList).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
            
    }
    
    private List<RegisterDataInfo> filter(List<RegisterDataInfo> infos, JsonQueryFilter filter) {
        Predicate<RegisterDataInfo> fromParams = getFilter(filter);
        return infos.stream().filter(fromParams).collect(Collectors.toList());
    }

    private Predicate<RegisterDataInfo> getFilter(JsonQueryFilter filter) {
        ImmutableList.Builder<Predicate<RegisterDataInfo>> list = ImmutableList.builder();
        if (filter.hasProperty("suspect")) {
            List<String> suspectFilters = filter.getStringList("suspect");
                        if (suspectFilters.size() == 0) {
                            if ("suspect".equals(filter.getString("suspect"))) {
                                list.add(this::hasSuspects);
                            } else {
                                list.add(not(this::hasSuspects));
                            }
                        }
        }
        return cdi -> list.build().stream().allMatch(p -> p.test(cdi));
    }
    
    private boolean hasSuspects(RegisterDataInfo info) {
        return ValidationStatus.SUSPECT.equals(info.validationResult);
    }

    private boolean filterActive(JsonQueryFilter filter, String key) {
        return filter.hasProperty(key) && filter.getBoolean(key);
    }

    private void calculateDeltaForNumericalReading(RegisterDataInfo previous, RegisterDataInfo current) {
        if (previous != null && current != null) {
            if (previous.value != null && current.value != null) {
                current.deltaValue = current.value.subtract(previous.value);
                current.deltaValue = current.deltaValue.setScale(current.value.scale(), BigDecimal.ROUND_UP);
            }
        }
    }
            
            
    @GET @Transactional
    @Path("/{timeStamp}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
//    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public Response getRegisterData(@PathParam("mrid") String mrid, @PathParam("rt_mrid") String rt_mrid, @PathParam("timeStamp") long timeStamp) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByMrIdOrThrowException(mrid);
        ReadingType readingType = resourceHelper.findReadingTypeByMrIdOrThrowException(rt_mrid);

        UsagePointValidation upv = registerHelper.getUsagePointValidation(usagePoint);
        boolean validationEnabled = upv.isValidationActive();
        Channel channel = registerHelper.findRegisterOnUsagePoint(mrid, rt_mrid)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_CHANNEL_FOR_USAGE_POINT_FOR_MRID, mrid, rt_mrid));
        
        BaseReadingRecord reading = channel.getReading(Instant.ofEpochMilli(timeStamp)).orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_READING_ON_REGISTER, rt_mrid, timeStamp));
        RegisterDataInfo readingInfo = usagePointDataInfoFactory.createRegisterDataInfo(reading, validationEnabled, channel, upv);
        return Response.ok(readingInfo).build();
    }
//
//    @PUT @Transactional
//    @Path("/{timeStamp}")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
//    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_DATA, Privileges.Constants.ADMINISTER_DECOMMISSIONED_DEVICE_DATA})
//    public Response editRegisterData(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, ReadingInfo readingInfo) {
//        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
//        Register<?> register = resourceHelper.findRegisterOrThrowException(device, registerId);
//        if((readingInfo instanceof NumericalReadingInfo && NumericalReadingInfo.class.cast(readingInfo).isConfirmed != null && NumericalReadingInfo.class.cast(readingInfo).isConfirmed) ||
//                (readingInfo instanceof BillingReadingInfo && BillingReadingInfo.class.cast(readingInfo).isConfirmed != null && BillingReadingInfo.class.cast(readingInfo).isConfirmed)) {
//            register.startEditingData().confirmReading(readingInfo.createNew(register)).complete();
//        } else {
//            register.startEditingData().editReading(readingInfo.createNew(register)).complete();
//        }
//        return Response.status(Response.Status.OK).build();
//    }
//
//
//    @POST @Transactional
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
//    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_DATA, Privileges.Constants.ADMINISTER_DECOMMISSIONED_DEVICE_DATA})
//    public Response addRegisterData(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, ReadingInfo readingInfo) {
//        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
//        Register<?> register = resourceHelper.findRegisterOrThrowException(device, registerId);
//        try {
//            register.startEditingData().editReading(readingInfo.createNew(register)).complete();
//        } catch (NoMeterActivationAt e) {
//            Instant time = (Instant) e.get("time");
//            throw this.exceptionFactory.newExceptionSupplier(MessageSeeds.CANT_ADD_READINGS_FOR_STATE, Date.from(time)).get();
//        }
//        return Response.status(Response.Status.OK).build();
//    }
//
//    @DELETE @Transactional
//    @Path("/{timeStamp}")
//    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
//    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_DATA, Privileges.Constants.ADMINISTER_DECOMMISSIONED_DEVICE_DATA})
//    public Response deleteRegisterData(@PathParam("mRID") String mRID, @PathParam("registerId") long registerId, @PathParam("timeStamp") long timeStamp, @BeanParam JsonQueryParameters queryParameters) {
//        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
//        Register<?> register = resourceHelper.findRegisterOrThrowException(device, registerId);
//        try {
//            register.startEditingData().removeReading(Instant.ofEpochMilli(timeStamp)).complete();
//        }
//        catch (IllegalArgumentException e) {
//            throw this.exceptionFactory.newExceptionSupplier(MessageSeeds.NO_CHANNELS_ON_REGISTER, registerId).get();
//        }
//        return Response.status(Response.Status.OK).build();
//    }

//    private boolean hasSuspects(ReadingInfo info) {
//        boolean result = true;
//        if (info instanceof BillingReadingInfo) {
//            BillingReadingInfo billingReadingInfo = (BillingReadingInfo)info;
//            result = ValidationStatus.SUSPECT.equals(billingReadingInfo.validationResult);
//        } else if (info instanceof NumericalReadingInfo) {
//            NumericalReadingInfo numericalReadingInfo = (NumericalReadingInfo)info;
//            result = ValidationStatus.SUSPECT.equals(numericalReadingInfo.validationResult);
//        }
//        return result;
//    }

//    private boolean hideSuspects(ReadingInfo info) {
//        return !hasSuspects(info);
//    }

//    private List<ReadingInfo> filter(List<ReadingInfo> infos, JsonQueryFilter filter) {
//        Predicate<ReadingInfo> fromParams = getFilter(filter);
//        return infos.stream().filter(fromParams).collect(Collectors.toList());
//    }
//
//    private Predicate<ReadingInfo> getFilter(JsonQueryFilter filter) {
//        ImmutableList.Builder<Predicate<ReadingInfo>> list = ImmutableList.builder();
//        if (filter.hasProperty("suspect")){
//            List<String> suspectFilters = filter.getStringList("suspect");
//            if (suspectFilters.size() == 0) {
//                if ("suspect".equals(filter.getString("suspect"))) {
//                    list.add(this::hasSuspects);
//                } else {
//                    list.add(not(this::hasSuspects));
//                }
//            }
//        }
//        if (filterActive(filter, "hideSuspects")) {
//            list.add(this::hideSuspects);
//        }
//        return lpi -> list.build().stream().allMatch(p -> p.test(lpi));
//    }
//
//    private boolean filterActive(JsonQueryFilter filter, String key) {
//        return filter.hasProperty(key) && filter.getBoolean(key);
//    }
}
