package com.elster.insight.usagepoint.data.rest.impl;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import com.elster.insight.usagepoint.config.UsagePointMetrologyConfiguration;
import com.elster.insight.usagepoint.data.UsagePointValidation;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.validation.ValidationEvaluator;
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
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.BROWSE_ANY, Privileges.Constants.BROWSE_OWN})
    public Response getChannels(@PathParam("mrid") String mRID, @BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        return channelHelper.get().getChannels(mRID, queryParameters);
    }

    @GET
    @Path("/{rt_mrid}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.BROWSE_ANY, Privileges.Constants.BROWSE_OWN})
    public Response getChannel(@PathParam("mrid") String mrid, @PathParam("rt_mrid") String rt_mrid) {
        Channel channel = channelHelper.get().findCurrentChannelOnUsagePoint(mrid, rt_mrid)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_CHANNEL_FOR_USAGE_POINT_FOR_MRID, mrid, rt_mrid));
        return channelHelper.get().getChannel(() -> channel);
    }

    @GET
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
            Optional<Meter> meterHolder = usagepoint.getMeter(clock.instant());

            final UsagePointValidation upv = channelHelper.get().getUsagePointValidation(usagepoint);
            final boolean validationEnabled = upv.isValidationActive();
            final Channel channel = channelHelper.get().findCurrentChannelOnUsagePoint(mrid, rt_mrid)
                    .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_CHANNEL_FOR_USAGE_POINT_FOR_MRID, mrid, rt_mrid));

            List<? extends BaseReadingRecord> channelData = usagepoint.getReadingsWithFill(range, readingType);
            List<? extends BaseReadingRecord> reversedChannelData = Lists.reverse(channelData);

            List<ChannelDataInfo> infos = transactionService.execute(
                    new Transaction<List<ChannelDataInfo>>() {
                        @Override
                        public List<ChannelDataInfo> perform() {
                            return reversedChannelData.stream().map(
                                    irr -> usagePointDataInfoFactory.createChannelDataInfo(irr, validationEnabled, channel, upv)).collect(Collectors.toList());
                        }
                    });

            infos.addAll(channelData.stream().map(
                    irr -> usagePointDataInfoFactory.createChannelDataInfo(irr, validationEnabled, channel, upv)).collect(Collectors.toList()));

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
        if (filter.hasProperty("suspect")) {
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

    private ValidationEvaluator getEvaluator(Meter meter) {
        return validationService.getEvaluator(meter, Range.atMost(clock.instant()));
    }

}
