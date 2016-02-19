package com.elster.insight.usagepoint.data.rest.impl;

import static com.elster.jupiter.util.streams.Predicates.not;
import static java.util.stream.Collectors.toList;

import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

import com.elster.insight.common.rest.ExceptionFactory;
import com.elster.insight.common.services.ListPager;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationService;
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
    private final ExceptionFactory exceptionFactory;
    private final ValidationService validationService;
    private final TransactionService transactionService;

    @Inject
    public ChannelResource(Provider<ChannelResourceHelper> channelHelper, ResourceHelper resourceHelper, MeteringService meteringService, ExceptionFactory exceptionFactory,
            UsagePointDataInfoFactory usagePointDataInfoFactory, Thesaurus thesaurus, Clock clock, ValidationService validationService, TransactionService transactionService) {
        this.channelHelper = channelHelper;
        this.resourceHelper = resourceHelper;
        this.meteringService = meteringService;
        this.exceptionFactory = exceptionFactory;
        this.usagePointDataInfoFactory = usagePointDataInfoFactory;
        this.thesaurus = thesaurus;
        this.clock = clock;
        this.validationService = validationService;
        this.transactionService = transactionService;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.BROWSE_ANY, Privileges.Constants.BROWSE_OWN})
    public Response getChannels(@PathParam("mrid") String mRID, @BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        return channelHelper.get().getChannels(mRID, queryParameters);
    }

    @GET
    @Transactional
    @Path("/{rt_mrid}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.BROWSE_ANY, Privileges.Constants.BROWSE_OWN})
    public Response getChannel(@PathParam("mrid") String mrid, @PathParam("rt_mrid") String rt_mrid) {
        UsagePoint usagepoint = resourceHelper.findUsagePointByMrIdOrThrowException(mrid);
        Channel channel = channelHelper.get().findCurrentChannelOnUsagePoint(mrid, rt_mrid)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_CHANNEL_FOR_USAGE_POINT_FOR_MRID, mrid, rt_mrid));
        return channelHelper.get().getChannel(() -> channel, usagepoint);
    }

    @GET
    @Transactional
    @Path("/{rt_mrid}/data")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.BROWSE_ANY, Privileges.Constants.BROWSE_OWN})
    public Response getChannelData(
            @PathParam("mrid") String mrid,
            @PathParam("rt_mrid") String rt_mrid,
            @BeanParam JsonQueryFilter filter,
            @BeanParam JsonQueryParameters queryParameters) {
        if (filter.hasProperty("intervalStart") && filter.hasProperty("intervalEnd")) {

            Range<Instant> range = Ranges.openClosed(filter.getInstant("intervalStart"), filter.getInstant("intervalEnd"));

            UsagePoint usagepoint = resourceHelper.findUsagePointByMrIdOrThrowException(mrid);
            ReadingType readingType = resourceHelper.findReadingTypeByMrIdOrThrowException(rt_mrid);

            UsagePointValidation upv = channelHelper.get().getUsagePointValidation(usagepoint);
            boolean validationEnabled = upv.isValidationActive();
            Channel channel = channelHelper.get().findCurrentChannelOnUsagePoint(mrid, rt_mrid)
                    .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_CHANNEL_FOR_USAGE_POINT_FOR_MRID, mrid, rt_mrid));

            List<? extends BaseReadingRecord> channelData = usagepoint.getReadingsWithFill(range, readingType);
            List<? extends BaseReadingRecord> reversedChannelData = Lists.reverse(channelData);

            List<ChannelDataInfo> infos = reversedChannelData.stream().map(
                                    irr -> usagePointDataInfoFactory.createChannelDataInfo(irr, validationEnabled, channel, upv)).collect(Collectors.toList());
                 

            infos = filter(infos, filter);
            List<ChannelDataInfo> paginatedChannelData = ListPager.of(infos).from(queryParameters).find();
            PagedInfoList pagedInfoList = PagedInfoList.fromPagedList("data", paginatedChannelData, queryParameters);
            return Response.ok(pagedInfoList).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }
    
    @PUT @Transactional
    @Path("/{rt_mrid}/data")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
//    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_DATA, Privileges.Constants.ADMINISTER_DECOMMISSIONED_DEVICE_DATA})
    public Response editChannelData(@PathParam("mrid") String mrid, @PathParam("rt_mrid") String rt_mrid, @BeanParam JsonQueryParameters queryParameters, List<ChannelDataInfo> channelDataInfos) {
        UsagePoint usagepoint = resourceHelper.findUsagePointByMrIdOrThrowException(mrid);
        ReadingType readingType = resourceHelper.findReadingTypeByMrIdOrThrowException(rt_mrid);
        Channel channel = channelHelper.get().findCurrentChannelOnUsagePoint(mrid, rt_mrid)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_CHANNEL_FOR_USAGE_POINT_FOR_MRID, mrid, rt_mrid));
        
        List<BaseReading> editedReadings = new ArrayList<>();
        List<BaseReading> editedBulkReadings = new ArrayList<>();
        List<BaseReading> confirmedReadings = new ArrayList<>();
        List<Instant> removeCandidates = new ArrayList<>();
        channelDataInfos.forEach((channelDataInfo) -> {
            if (!(isToBeConfirmed(channelDataInfo)) && channelDataInfo.value == null && channelDataInfo.collectedValue == null) {
                removeCandidates.add(Instant.ofEpochMilli(channelDataInfo.interval.end));
            } else {
                if (channelDataInfo.value != null) {
                    editedReadings.add(channelDataInfo.createNew());
                }
                if (channelDataInfo.collectedValue != null) {
                    editedBulkReadings.add(channelDataInfo.createNewBulk());
                }
                if (isToBeConfirmed(channelDataInfo)) {
                    confirmedReadings.add(channelDataInfo.createConfirm());
                }
            }
        });
        
        
        channel.editReadings(editedReadings);
        channel.confirmReadings(confirmedReadings);
        
        removeCandidates.forEach(instant -> {
            channelHelper.get().findChannel(usagepoint, instant, rt_mrid).ifPresent(koreChannel -> {
                koreChannel.removeReadings(koreChannel.getReading(instant).map(Stream::of).orElseGet(Stream::empty).collect(toList()));
            });
        });
        
        return Response.status(Response.Status.OK).build();
    }
    
    private boolean isToBeConfirmed(ChannelDataInfo channelDataInfo) {
        return ((channelDataInfo.mainValidationInfo != null && channelDataInfo.mainValidationInfo.isConfirmed) ||
                (channelDataInfo.bulkValidationInfo != null && channelDataInfo.bulkValidationInfo.isConfirmed));
    }
    
    @PUT @Transactional
    @Path("{rt_mrid}/validate")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(com.elster.jupiter.validation.security.Privileges.Constants.VALIDATE_MANUAL)
    public Response validateDeviceData(UsagePointTriggerValidationInfo info, @PathParam("mrid") String mrid, @PathParam("rt_mrid") String rt_mrid) {
        UsagePoint usagePoint = resourceHelper.findUsagePointByMrIdOrThrowException(mrid);
        Channel channel = channelHelper.get().findCurrentChannelOnUsagePoint(mrid, rt_mrid)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_CHANNEL_FOR_USAGE_POINT_FOR_MRID, mrid, rt_mrid));
        
        UsagePointValidation upv = channelHelper.get().getUsagePointValidation(usagePoint);
        resourceHelper.lockUsagePointOrThrowException(usagePoint.getId(), info.version, usagePoint.getName());
        if (info.lastChecked != null) {
            upv.setLastChecked(channel, Instant.ofEpochMilli(info.lastChecked));
        }
        usagePoint.update();
        upv.validateChannel(channel);
        return Response.ok().build();
    }
    
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{rt_mrid}/data/{epochMillis}/validation")
    public Response getValidationData(
            @PathParam("mrid") String mrid,
            @PathParam("rt_mrid") String rt_mrid,
            @PathParam("epochMillis") long epochMillis) {
        UsagePoint usagepoint = resourceHelper.findUsagePointByMrIdOrThrowException(mrid);
        ReadingType readingType = resourceHelper.findReadingTypeByMrIdOrThrowException(rt_mrid);
        Channel channel = channelHelper.get().findCurrentChannelOnUsagePoint(mrid, rt_mrid)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_CHANNEL_FOR_USAGE_POINT_FOR_MRID, mrid, rt_mrid));
        
        UsagePointValidation upv = channelHelper.get().getUsagePointValidation(usagepoint);
        
        Instant to = Instant.ofEpochMilli(epochMillis);
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(to, clock.getZone());
        Instant from = zonedDateTime.minus(channel.getIntervalLength().get()).toInstant();
        Optional<? extends BaseReadingRecord> reading = usagepoint.getReadings(Range.openClosed(from, to), readingType).stream().findAny();
        

        Optional<VeeReadingInfo> veeReadingInfo = Optional.empty();
        
        if (reading.isPresent()) {
            IntervalReadingRecord realReading = (IntervalReadingRecord) reading.get();
            Optional<DataValidationStatus> tatuses = upv.getValidationStatus(channel, Arrays.asList(realReading), Range.openClosed(from, to)).stream().findFirst();
            if (tatuses.isPresent()) {
            veeReadingInfo = Optional.of(usagePointDataInfoFactory.createVeeReadingInfoWithModificationFlags(channel, tatuses.get(), upv, realReading));
            }
        }
        
        return Response.ok(veeReadingInfo.orElseGet(VeeReadingInfo::new)).build();
    }

    private List<ChannelDataInfo> filter(List<ChannelDataInfo> infos, JsonQueryFilter filter) {
        Predicate<ChannelDataInfo> fromParams = getFilter(filter);
        return infos.stream().filter(fromParams).collect(Collectors.toList());
    }

    private Predicate<ChannelDataInfo> getFilter(JsonQueryFilter filter) {
        ImmutableList.Builder<Predicate<ChannelDataInfo>> list = ImmutableList.builder();
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
        if (filterActive(filter, "hideMissing")) {
            list.add(this::hasMissingData);
        }
        return cdi -> list.build().stream().allMatch(p -> p.test(cdi));
    }

    private boolean filterActive(JsonQueryFilter filter, String key) {
        return filter.hasProperty(key) && filter.getBoolean(key);
    }
    
    private boolean hasSuspects(ChannelDataInfo info) {
        boolean hasSuspect = false;
        if (info.mainValidationInfo != null) {
            hasSuspect = ValidationStatus.SUSPECT.equals(info.mainValidationInfo.validationResult);
        }
        if (!hasSuspect && info.bulkValidationInfo != null) {
            hasSuspect = ValidationStatus.SUSPECT.equals(info.bulkValidationInfo.validationResult);
        }
        
        return hasSuspect;
    }

    private boolean hasMissingData(ChannelDataInfo info) {
        return info.value == null;
    }
    
}
