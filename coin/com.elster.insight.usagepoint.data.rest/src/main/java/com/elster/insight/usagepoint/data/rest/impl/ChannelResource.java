package com.elster.insight.usagepoint.data.rest.impl;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.elster.insight.common.services.ListPager;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.util.Ranges;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;



public class ChannelResource {
    
    private final MeteringService meteringService;
    
    
    private final Provider<ChannelResourceHelper> channelHelper;
    private final ResourceHelper resourceHelper;
    private final Thesaurus thesaurus;
    private final Clock clock;
    private final UsagePointDataInfoFactory usagePointDataInfoFactory;
//    private final IssueDataValidationService issueDataValidationService;
//    private final EstimationHelper estimationHelper;

    @Inject
    public ChannelResource(Provider<ChannelResourceHelper> channelHelper, ResourceHelper resourceHelper, MeteringService meteringService, UsagePointDataInfoFactory usagePointDataInfoFactory, Thesaurus thesaurus, Clock clock) {
        this.channelHelper = channelHelper;
        this.resourceHelper = resourceHelper;
        this.meteringService = meteringService;
        this.usagePointDataInfoFactory = usagePointDataInfoFactory;
        this.thesaurus = thesaurus;
        this.clock = clock;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
//    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.ADMINISTRATE_DEVICE_DATA, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.OPERATE_DEVICE_COMMUNICATION})
    public Response getChannels(@PathParam("mrid") String mRID, @BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        return channelHelper.get().getChannels(mRID, queryParameters);
    }

    @GET
    @Path("/{rt_mrid}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
//    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.ADMINISTRATE_DEVICE_DATA, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.OPERATE_DEVICE_COMMUNICATION})
    public Response getChannel(@PathParam("mrid") String mrid, @PathParam("rt_mrid") String rt_mrid) {
        Channel channel = channelHelper.get().findCurrentChannelOnUsagePoint(mrid, rt_mrid); 
        return channelHelper.get().getChannel(() -> channel);
    }

    @GET
    @Path("/{rt_mrid}/data")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
//    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.ADMINISTRATE_DEVICE_DATA, Privileges.ADMINISTER_DECOMMISSIONED_DEVICE_DATA})
    public Response getChannelData(
            @PathParam("mrid") String mrid,
            @PathParam("rt_mrid") String rt_mrid,
            @BeanParam JsonQueryFilter filter,
            @BeanParam JsonQueryParameters queryParameters) {
        if (filter.hasProperty("intervalStart") && filter.hasProperty("intervalEnd")) {
            
            Range<Instant> range = Ranges.openClosed(filter.getInstant("intervalStart"), filter.getInstant("intervalEnd"));
        
            
            UsagePoint usagepoint = resourceHelper.findUsagePointByMrIdOrThrowException(mrid);
            ReadingType readingType = resourceHelper.findReadingTypeByMrIdOrThrowException(rt_mrid);
            
            List<? extends BaseReadingRecord> channelData = usagepoint.getReadingsWithFill(range, readingType);
            channelData = Lists.reverse(channelData);
            List<ChannelDataInfo> infos = new ArrayList<>();
            infos.addAll(channelData.stream().map(
                    irr -> usagePointDataInfoFactory.createChannelDataInfo(irr)).
                    collect(Collectors.toList()));
            
            
            infos = filter(infos, filter);
            List<ChannelDataInfo> paginatedChannelData = ListPager.of(infos).from(queryParameters).find();
            PagedInfoList pagedInfoList = PagedInfoList.fromPagedList("data", paginatedChannelData, queryParameters);
            return Response.ok(pagedInfoList).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    private List<ChannelDataInfo> filter(List<ChannelDataInfo> infos, JsonQueryFilter filter) {
        Predicate<ChannelDataInfo> fromParams = getFilter(filter);
        return infos.stream().filter(fromParams).collect(Collectors.toList());
    }

    private Predicate<ChannelDataInfo> getFilter(JsonQueryFilter filter) {
        ImmutableList.Builder<Predicate<ChannelDataInfo>> list = ImmutableList.builder();
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
        if (filterActive(filter, "hideMissing")) {
            list.add(this::hasMissingData);
        }
        return cdi -> list.build().stream().allMatch(p -> p.test(cdi));
    }

    private boolean filterActive(JsonQueryFilter filter, String key) {
        return filter.hasProperty(key) && filter.getBoolean(key);
    }

    private boolean hasMissingData(ChannelDataInfo info) {
        return info.value == null;
    }

}
