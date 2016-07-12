package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.streams.Functions;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Path("/usagepoints")
public class UsagePointResource {

    private final MeteringService meteringService;
    private final ExceptionFactory exceptionFactory;
    private final MeterInfoFactory meterInfoFactory;
    private final UsagePointChannelInfoFactory usagePointChannelInfoFactory;

    @Inject
    public UsagePointResource(MeteringService meteringService, ExceptionFactory exceptionFactory, MeterInfoFactory meterInfoFactory, UsagePointChannelInfoFactory usagePointChannelInfoFactory) {
        this.meteringService = meteringService;
        this.exceptionFactory = exceptionFactory;
        this.meterInfoFactory = meterInfoFactory;
        this.usagePointChannelInfoFactory = usagePointChannelInfoFactory;
    }

    @GET
    @Path("/{mRID}/history/devices")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    public PagedInfoList getDevicesHistory(@PathParam("mRID") String mRID, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = fetchUsagePoint(mRID);
        return PagedInfoList.fromCompleteList("devices", meterInfoFactory.getDevicesHistory(usagePoint), queryParameters);
    }

    @GET
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    @Path("/{mRID}/channels")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public PagedInfoList getChannels(@PathParam("mRID") String mRID, @Context SecurityContext securityContext, @BeanParam JsonQueryParameters queryParameters) {
        List<UsagePointChannelInfo> channelInfos = new ArrayList<>();
        UsagePoint usagePoint = fetchUsagePoint(mRID);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration = usagePoint.getEffectiveMetrologyConfiguration().orElse(null);
        if (effectiveMetrologyConfiguration != null) {
            UsagePointMetrologyConfiguration metrologyConfiguration = effectiveMetrologyConfiguration.getMetrologyConfiguration();
            channelInfos = metrologyConfiguration.getContracts().stream()
                    .map(effectiveMetrologyConfiguration::getChannelsContainer)
                    .flatMap(Functions.asStream())
                    .flatMap(channelsContainer -> channelsContainer.getChannels().stream())
                    .map(channel -> usagePointChannelInfoFactory.from(channel, usagePoint, metrologyConfiguration))
                    .collect(Collectors.toList());
        }
        return PagedInfoList.fromPagedList("channels", channelInfos, queryParameters);
    }

    private UsagePoint fetchUsagePoint(String mRID) {
        return meteringService.findUsagePoint(mRID)
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_USAGE_POINT_FOR_MRID, mRID));
    }
}
