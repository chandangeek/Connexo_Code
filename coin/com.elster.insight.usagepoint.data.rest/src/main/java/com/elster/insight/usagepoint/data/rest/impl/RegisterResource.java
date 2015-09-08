package com.elster.insight.usagepoint.data.rest.impl;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.elster.insight.common.rest.ExceptionFactory;
import com.elster.insight.common.services.ListPager;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.util.Ranges;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

public class RegisterResource {

    private final Provider<RegisterResourceHelper> registerHelper;
    private final MeteringService meteringService;
    private final ResourceHelper resourceHelper;
    private final UsagePointDataInfoFactory usagePointDataInfoFactory;
    private final Clock clock;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public RegisterResource(Provider<RegisterResourceHelper> registerHelper, ResourceHelper resourceHelper, MeteringService meteringService, ExceptionFactory exceptionFactory, Clock clock, UsagePointDataInfoFactory usagePointDataInfoFactory) {
        this.registerHelper = registerHelper;
        this.resourceHelper = resourceHelper;
        this.meteringService = meteringService;
        this.exceptionFactory = exceptionFactory;
        this.clock = clock;
        this.usagePointDataInfoFactory = usagePointDataInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.BROWSE_ANY, Privileges.BROWSE_OWN})
    public Response getRegisters(@PathParam("mrid") String mrid, @BeanParam JsonQueryParameters queryParameters) {
        return registerHelper.get().getRegisters(mrid, queryParameters);
    }

    @GET
    @Path("/{rt_mrid}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.BROWSE_ANY, Privileges.BROWSE_OWN})
    public Response getRegister(@PathParam("mrid") String mrid, @PathParam("rt_mrid") String rt_mrid) {
        Channel channel = registerHelper.get().findRegisterOnUsagePoint(mrid, rt_mrid).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_REGISTER_FOR_USAGE_POINT_FOR_MRID, mrid, rt_mrid)); 
        return registerHelper.get().getRegister(() -> channel);
    }
    
    @GET
    @Path("/{rt_mrid}/data")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.BROWSE_ANY, Privileges.BROWSE_OWN})
    public Response getRegisterData(
            @PathParam("mrid") String mrid,
            @PathParam("rt_mrid") String rt_mrid,
            @BeanParam JsonQueryFilter filter,
            @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByMrIdOrThrowException(mrid);
        ReadingType readingType = resourceHelper.findReadingTypeByMrIdOrThrowException(rt_mrid);
        if (filter.hasProperty("intervalStart") && filter.hasProperty("intervalEnd")) {
            
            Range<Instant> range = Ranges.openClosed(filter.getInstant("intervalStart"), filter.getInstant("intervalEnd"));
            List<? extends BaseReadingRecord> registerDataInfo = usagePoint.getReadings(range, readingType);
            List<RegisterDataInfo> infos = registerDataInfo.stream().map(
                    readingRecord -> usagePointDataInfoFactory.createRegisterDataInfo(readingRecord)).
                    collect(Collectors.toList());
            
            
            Collections.sort(infos, (ri1, ri2) -> ri2.readingTime.compareTo(ri1.readingTime));
            /* And fill a delta value for cumulative reading type. The delta is the difference with the previous record.
               The Delta value won't be stored in the database yet, as it has a performance impact */
            if (readingType.isCumulative()){
                for (int i = 0; i < infos.size() - 1; i++){
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
        if (filter.hasProperty("suspect")){
            List<String> suspectFilters = filter.getStringList("suspect");
//            if (suspectFilters.size() == 0) {
//                if ("suspect".equals(filter.getString("suspect"))) {
//                    list.add(this::hasSuspects);
//                } else {
//                    list.add(not(this::hasSuspects));
//                }
//            }
        }
        return cdi -> list.build().stream().allMatch(p -> p.test(cdi));
    }
    
    private boolean filterActive(JsonQueryFilter filter, String key) {
        return filter.hasProperty(key) && filter.getBoolean(key);
    }
    
    private void calculateDeltaForNumericalReading(RegisterDataInfo previous, RegisterDataInfo current){
        if (previous != null && current != null ){
            if (previous.value != null && current.value != null) {
                current.deltaValue = current.value.subtract(previous.value);
                current.deltaValue = current.deltaValue.setScale(current.value.scale(), BigDecimal.ROUND_UP);
            }
        }
    }
    
}