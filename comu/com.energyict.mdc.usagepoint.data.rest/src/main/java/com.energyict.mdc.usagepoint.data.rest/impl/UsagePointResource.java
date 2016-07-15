package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.common.services.ListPager;

import com.google.common.collect.Range;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Path("/usagepoints")
public class UsagePointResource {

    private final MeterInfoFactory meterInfoFactory;
    private final UsagePointChannelInfoFactory usagePointChannelInfoFactory;
    private final ResourceHelper resourceHelper;
    private final ChannelDataInfoFactory channelDataInfoFactory;
    private final ValidationService validationService;

    @Inject
    public UsagePointResource(MeterInfoFactory meterInfoFactory,
                              UsagePointChannelInfoFactory usagePointChannelInfoFactory,
                              ResourceHelper resourceHelper,
                              ChannelDataInfoFactory channelDataInfoFactory,
                              ValidationService validationService) {
        this.meterInfoFactory = meterInfoFactory;
        this.usagePointChannelInfoFactory = usagePointChannelInfoFactory;
        this.resourceHelper = resourceHelper;
        this.channelDataInfoFactory = channelDataInfoFactory;
        this.validationService = validationService;
    }

    @GET
    @Path("/{mRID}/history/devices")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    public PagedInfoList getDevicesHistory(@PathParam("mRID") String mRID, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.fetchUsagePoint(mRID);
        return PagedInfoList.fromCompleteList("devices", meterInfoFactory.getDevicesHistory(usagePoint), queryParameters);
    }

    @GET
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    @Path("/{mRID}/channels")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public PagedInfoList getChannels(@PathParam("mRID") String mRID, @Context SecurityContext securityContext, @BeanParam JsonQueryParameters queryParameters) {
        List<UsagePointChannelInfo> channelInfos = new ArrayList<>();
        UsagePoint usagePoint = resourceHelper.fetchUsagePoint(mRID);
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

    @GET
    @Transactional
    @Path("/{mRID}/channels/{channelid}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.Constants.VIEW_DEVICE, com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_DATA, com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTER_DECOMMISSIONED_DEVICE_DATA})
    public UsagePointChannelInfo getChannel(
            @PathParam("mRID") String mRID,
            @PathParam("channelid") long channelId) {

        UsagePoint usagePoint = resourceHelper.fetchUsagePoint(mRID);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration = resourceHelper.getEffectiveMetrologyConfigurationOnUsagePoint(usagePoint);
        Channel channel = resourceHelper.findChannelOnUsagePointOrThrowException(effectiveMetrologyConfiguration, channelId);

        return usagePointChannelInfoFactory.from(channel, usagePoint, effectiveMetrologyConfiguration.getMetrologyConfiguration());
    }

    @GET
    @Transactional
    @Path("/{mRID}/channels/{channelid}/data")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.Constants.VIEW_DEVICE, com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_DATA, com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTER_DECOMMISSIONED_DEVICE_DATA})
    public Response getChannelData(
            @PathParam("mRID") String mRID,
            @PathParam("channelid") long channelId,
            @BeanParam JsonQueryFilter filter,
            @BeanParam JsonQueryParameters queryParameters) {

        Channel channel = resourceHelper.findChannelOnUsagePointOrThrowException(mRID, channelId);

        if (filter.hasProperty("intervalStart") && filter.hasProperty("intervalEnd")) {
            Range<Instant> range = Ranges.openClosed(filter.getInstant("intervalStart"), filter.getInstant("intervalEnd"));
            List<IntervalReadingRecord> intervalReadings = channel.getIntervalReadings(range);

            List<DataValidationStatus> validationStatuses = validationService.getEvaluator()
                    .getValidationStatus(Collections.singleton(QualityCodeSystem.MDC), channel, intervalReadings, range);

            List<ChannelDataInfo> infos = intervalReadings.stream()
                    .map(intervalReading -> channelDataInfoFactory.createChannelDataInfo(intervalReading, validationStatuses, false))
                    .collect(Collectors.toList());

            List<ChannelDataInfo> paginatedChannelData = ListPager.of(infos).from(queryParameters).find();
            PagedInfoList pagedInfoList = PagedInfoList.fromPagedList("data", paginatedChannelData, queryParameters);
            return Response.ok(pagedInfoList).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @GET
    @Transactional
    @Path("/{mRID}/channels/{channelid}/data/{epochMillis}/validation")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.Constants.VIEW_DEVICE, com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_DATA, com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTER_DECOMMISSIONED_DEVICE_DATA})
    public ChannelDataInfo getChannelDataReading(
            @PathParam("mRID") String mRID,
            @PathParam("channelid") long channelId,
            @PathParam("epochMillis") long epochMillis,
            @BeanParam JsonQueryFilter filter,
            @BeanParam JsonQueryParameters queryParameters) {

        Channel channel = resourceHelper.findChannelOnUsagePointOrThrowException(mRID, channelId);

        Range<Instant> range = Ranges.openClosed(Instant.ofEpochMilli(epochMillis - 1), Instant.ofEpochMilli(epochMillis));
        List<IntervalReadingRecord> intervalReadings = channel.getIntervalReadings(range);

        List<DataValidationStatus> validationStatuses = validationService.getEvaluator()
                .getValidationStatus(Collections.singleton(QualityCodeSystem.MDC), channel, intervalReadings, range);

        return channelDataInfoFactory.createChannelDataInfo(resourceHelper.findReadingOrThrowException(intervalReadings), validationStatuses, true);
    }
}
