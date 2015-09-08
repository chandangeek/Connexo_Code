package com.elster.insight.usagepoint.data.rest.impl;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import com.elster.insight.common.rest.ExceptionFactory;
import com.elster.insight.common.services.ListPager;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;

public class ChannelResourceHelper {

    private static final Comparator<Channel> CHANNEL_COMPARATOR_BY_NAME = new ChannelComparator();

    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final Clock clock;

    @Inject
    public ChannelResourceHelper(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory, Clock clock) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.clock = clock;
    }

    public Response getChannels(String mrid, JsonQueryParameters queryParameters) {
        List<Channel> regularChannels = new ArrayList<Channel>();

        UsagePoint usagepoint = resourceHelper.findUsagePointByMrIdOrThrowException(mrid);
        MeterActivation currentActivation = usagepoint.getCurrentMeterActivation().orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_CURRENT_ACTIVATION_FOR_USAGE_POINT_FOR_MRID, mrid));
        List<Channel> channelCandidates = currentActivation.getChannels();
        for (Channel channel : channelCandidates) {
            if (channel.isRegular())
                regularChannels.add(channel);
        }
        List<Channel> channelsPage = ListPager.of(regularChannels, CHANNEL_COMPARATOR_BY_NAME).from(queryParameters).find();

        List<ChannelInfo> channelInfos = new ArrayList<>();
        for (Channel channel : channelsPage) {
            ChannelInfo channelInfo = ChannelInfo.from(channel);
            channelInfos.add(channelInfo);
        }
        return Response.ok(PagedInfoList.fromPagedList("channels", channelInfos, queryParameters)).build();
    }

    public Optional<Channel> findCurrentChannelOnUsagePoint(String mrid, String rt_mrid) {
        UsagePoint usagepoint = resourceHelper.findUsagePointByMrIdOrThrowException(mrid);
        MeterActivation currentActivation = usagepoint.getCurrentMeterActivation().orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_CURRENT_ACTIVATION_FOR_USAGE_POINT_FOR_MRID, mrid));
        List<Channel> channelCandidates = currentActivation.getChannels();
        for (Channel channel : channelCandidates) {
            if (rt_mrid.equals(channel.getMainReadingType().getMRID())) {
                return Optional.of(channel);
            }
        }
        return Optional.empty();
    }

    public Response getChannel(Supplier<Channel> channelSupplier) {
        Channel channel = channelSupplier.get();
        ChannelInfo channelInfo = ChannelInfo.from(channel);
        return Response.ok(channelInfo).build();
    }

}