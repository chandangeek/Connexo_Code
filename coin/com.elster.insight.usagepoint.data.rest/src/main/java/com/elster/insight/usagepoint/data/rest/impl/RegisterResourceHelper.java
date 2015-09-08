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

public class RegisterResourceHelper {

    private static final Comparator<Channel> CHANNEL_COMPARATOR_BY_NAME = new ChannelComparator();

    private final ResourceHelper resourceHelper;
    private final Clock clock;

    private ExceptionFactory exceptionFactory;

    @Inject
    public RegisterResourceHelper(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory, Clock clock) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.clock = clock;
    }

    public Response getRegisters(String mrid, JsonQueryParameters queryParameters) {
        List<Channel> irregularChannels = new ArrayList<Channel>();
        UsagePoint usagepoint = resourceHelper.findUsagePointByMrIdOrThrowException(mrid);
        MeterActivation currentActivation = usagepoint.getCurrentMeterActivation().orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_CURRENT_ACTIVATION_FOR_USAGE_POINT_FOR_MRID, mrid));
        List<Channel> channelCandidates = currentActivation.getChannels();
        for (Channel channel : channelCandidates) {
            if (!channel.isRegular())
                irregularChannels.add(channel);
        }
        List<Channel> channelsPage = ListPager.of(irregularChannels, CHANNEL_COMPARATOR_BY_NAME).from(queryParameters).find();

        List<RegisterInfo> registerInfos = new ArrayList<>();
        for (Channel channel : channelsPage) {
            RegisterInfo registerInfo = RegisterInfo.from(channel);
            registerInfos.add(registerInfo);
        }
        return Response.ok(PagedInfoList.fromPagedList("registers", registerInfos, queryParameters)).build();
    }

    public Optional<Channel> findRegisterOnUsagePoint(String mrid, String rt_mrid) {
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

    public Response getRegister(Supplier<Channel> channelSupplier) {
        Channel channel = channelSupplier.get();
        RegisterInfo registerInfo = RegisterInfo.from(channel);
        return Response.ok(registerInfo).build();
    }

}