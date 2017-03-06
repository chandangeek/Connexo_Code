/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeRequirementsCollector;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeMap;
import com.google.common.collect.TreeRangeSet;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Path("/usagepoints")
public class UsagePointResource {

    private final ValidationService validationService;
    private final MeterInfoFactory meterInfoFactory;
    private final ResourceHelper resourceHelper;
    private final ChannelDataInfoFactory channelDataInfoFactory;
    private final UsagePointChannelInfoFactory usagePointChannelInfoFactory;
    private final UsagePointRegisterInfoFactory usagePointRegisterInfoFactory;
    private final ExceptionFactory exceptionFactory;
    private final Clock clock;

    @Inject
    public UsagePointResource(MeterInfoFactory meterInfoFactory,
                              UsagePointChannelInfoFactory usagePointChannelInfoFactory,
                              UsagePointRegisterInfoFactory usagePointRegisterInfoFactory,
                              ResourceHelper resourceHelper,
                              ChannelDataInfoFactory channelDataInfoFactory,
                              ValidationService validationService,
                              ExceptionFactory exceptionFactory,
                              Clock clock) {
        this.meterInfoFactory = meterInfoFactory;
        this.usagePointChannelInfoFactory = usagePointChannelInfoFactory;
        this.usagePointRegisterInfoFactory = usagePointRegisterInfoFactory;
        this.resourceHelper = resourceHelper;
        this.channelDataInfoFactory = channelDataInfoFactory;
        this.validationService = validationService;
        this.exceptionFactory = exceptionFactory;
        this.clock = clock;
    }

    @GET
    @Path("/{name}/history/devices")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT, Privileges.Constants.ADMINISTER_OWN_USAGEPOINT})
    public PagedInfoList getDevicesHistory(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointOrThrowException(name);
        return PagedInfoList.fromCompleteList("devices", meterInfoFactory.getDevicesHistory(usagePoint), queryParameters);
    }

    @GET
    @Path("/{name}/channels")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Transactional
    public PagedInfoList getChannels(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters) {
        List<UsagePointChannelInfo> channelInfos = getInfos(name, usagePointChannelInfoFactory);
        return PagedInfoList.fromPagedList("channels", channelInfos, queryParameters);
    }

    @GET
    @Path("/{name}/registers")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Transactional
    public PagedInfoList getRegisters(@PathParam("name") String name, @BeanParam JsonQueryParameters queryParameters) {
        List<UsagePointRegisterInfo> registerInfos = getInfos(name, usagePointRegisterInfoFactory);
        return PagedInfoList.fromPagedList("registers", registerInfos, queryParameters);
    }

    private <T,I> List<T> getInfos(String usagePointName, AbstractUsagePointChannelInfoFactory<T, I> factory) {
        List<T> infos = new ArrayList<>();
        UsagePoint usagePoint = resourceHelper.findUsagePointOrThrowException(usagePointName);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration = usagePoint.getCurrentEffectiveMetrologyConfiguration()
                .orElse(null);
        if (effectiveMetrologyConfiguration != null) {
            UsagePointMetrologyConfiguration metrologyConfiguration = effectiveMetrologyConfiguration.getMetrologyConfiguration();
            infos = metrologyConfiguration.getContracts().stream()
                    .map(effectiveMetrologyConfiguration::getChannelsContainer)
                    .flatMap(Functions.asStream())
                    .flatMap(channelsContainer -> channelsContainer.getChannels().stream())
                    .filter(factory.getRegisterFilter())
                    .map(channel -> factory.from(channel, usagePoint, metrologyConfiguration))
                    .collect(Collectors.toList());
        }
        return infos;
    }

    @GET
    @Path("/{name}/channels/{channelId}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT, Privileges.Constants.ADMINISTER_OWN_USAGEPOINT})
    public UsagePointChannelInfo getChannel(@PathParam("name") String name, @PathParam("channelId") long channelId) {
        UsagePoint usagePoint = resourceHelper.findUsagePointOrThrowException(name);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration = usagePoint.getCurrentEffectiveMetrologyConfiguration()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_METROLOGY_CONFIG_FOR_USAGE_POINT, usagePoint
                        .getName()));
        Channel channel = resourceHelper.findChannelOnUsagePointOrThrowException(effectiveMetrologyConfiguration, channelId);
        return usagePointChannelInfoFactory.from(channel, usagePoint, effectiveMetrologyConfiguration.getMetrologyConfiguration());
    }

    @GET
    @Path("/{name}/channels/{channelId}/data")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT, Privileges.Constants.ADMINISTER_OWN_USAGEPOINT})
    public PagedInfoList getChannelData(@PathParam("name") String name, @PathParam("channelId") long channelId, @BeanParam JsonQueryFilter filter, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointOrThrowException(name);
        List<ChannelDataInfo> outputChannelDataInfoList = Collections.emptyList();
        if (filter.hasProperty("intervalStart") && filter.hasProperty("intervalEnd")) {
            Range<Instant> requestedInterval = Ranges.openClosed(filter.getInstant("intervalStart"), filter.getInstant("intervalEnd"));
            EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration = usagePoint.getCurrentEffectiveMetrologyConfiguration()
                    .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_METROLOGY_CONFIG_FOR_USAGE_POINT, usagePoint
                            .getName()));
            Channel channel = resourceHelper.findChannelOnUsagePointOrThrowException(effectiveMetrologyConfiguration, channelId);
            Range<Instant> usagePointActivationInterval = getUsagePointActivationInterval(usagePoint);
            if (usagePointActivationInterval.isConnected(requestedInterval)) {
                Range<Instant> effectiveInterval = usagePointActivationInterval.intersection(requestedInterval);
                TemporalAmount intervalLength = channel.getIntervalLength().get();
                Map<Instant, IntervalReadingWithValidationStatus> preFilledChannelDataMap = channel.toList(effectiveInterval)
                        .stream()
                        .collect(Collectors.toMap(Function.identity(), timeStamp -> new IntervalReadingWithValidationStatus(ZonedDateTime
                                .ofInstant(timeStamp, clock.getZone()), intervalLength)));

                // add readings to pre filled channel data map
                List<IntervalReadingRecord> intervalReadings = channel.getIntervalReadings(effectiveInterval);
                for (IntervalReadingRecord intervalReadingRecord : intervalReadings) {
                    IntervalReadingWithValidationStatus readingHolder = preFilledChannelDataMap.get(intervalReadingRecord
                            .getTimeStamp());
                    if (readingHolder != null) {
                        readingHolder.setIntervalReadingRecord(intervalReadingRecord);
                    }
                }
                RangeMap<Instant, Instant> lastCheckedOfSourceChannels = getLastCheckedOfSourceChannels(effectiveMetrologyConfiguration, channel);
                outputChannelDataInfoList = preFilledChannelDataMap.entrySet().stream()
                        .sorted(Collections.reverseOrder(Comparator.comparing(Map.Entry::getKey)))
                        .map(Map.Entry::getValue)
                        .map(reading -> channelDataInfoFactory.asInfo(reading, lastCheckedOfSourceChannels))
                        .collect(Collectors.toList());
            }
        }
        return PagedInfoList.fromCompleteList("data", outputChannelDataInfoList, queryParameters);
    }

    private Range<Instant> getUsagePointActivationInterval(UsagePoint usagePoint) {
        RangeSet<Instant> meterActivationIntervals = usagePoint.getMeterActivations().stream()
                .map(MeterActivation::getRange)
                .collect(TreeRangeSet::<Instant>create, RangeSet::add, RangeSet::addAll);
        return !meterActivationIntervals.isEmpty() ? meterActivationIntervals.span() : Range.singleton(Instant.MIN);
    }

    private RangeMap<Instant, Instant> getLastCheckedOfSourceChannels(EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration, Channel aggregatedChannel) {
        Optional<ReadingTypeRequirement> readingTypeRequirement = getReadingTypeRequirement(effectiveMetrologyConfiguration, aggregatedChannel);
        RangeMap<Instant, Instant> rangeMap = TreeRangeMap.create();
        readingTypeRequirement.ifPresent(requirement ->
                findSourceChannelsOnUsagePoint(requirement, effectiveMetrologyConfiguration.getUsagePoint())
                        .forEach(channel -> rangeMap.put(getRange(channel), getLastChecked(channel)))
        );
        return rangeMap;
    }

    private Optional<ReadingTypeRequirement> getReadingTypeRequirement(EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration, Channel channel) {
        MetrologyContract contract = resourceHelper.findMetrologyContractOfChannelOrThrowException(effectiveMetrologyConfiguration, channel
                .getId());
        Optional<ReadingTypeDeliverable> readingTypeDeliverable = contract.getDeliverables().stream()
                .filter(deliverable -> deliverable.getReadingType().equals(channel.getMainReadingType()))
                .findFirst();
        ReadingTypeRequirementsCollector readingTypeRequirementsCollector = new ReadingTypeRequirementsCollector();
        readingTypeDeliverable.get().getFormula().getExpressionNode().accept(readingTypeRequirementsCollector);
        List<ReadingTypeRequirement> readingTypeRequirements = readingTypeRequirementsCollector.getReadingTypeRequirements();
        if (!readingTypeRequirements.isEmpty()) {
            FullySpecifiedReadingTypeRequirement readingTypeRequirement = (FullySpecifiedReadingTypeRequirement) readingTypeRequirements
                    .get(0);// expecting max 1 requirement
            return Optional.of(readingTypeRequirement);
        }
        return Optional.empty();
    }

    private List<Channel> findSourceChannelsOnUsagePoint(ReadingTypeRequirement readingTypeRequirement, UsagePoint usagePoint) {
        return usagePoint.getMeterActivations().stream()
                .flatMap(meterActivation -> readingTypeRequirement.getMatchingChannelsFor(meterActivation.getChannelsContainer())
                        .stream())
                .collect(Collectors.toList());
    }

    private Range<Instant> getRange(Channel channel) {
        return Ranges.copy(channel.getChannelsContainer().getRange()).asOpenClosed();
    }

    private Instant getLastChecked(Channel channel) {
        Meter meter = channel.getChannelsContainer().getMeter().get();
        return validationService.getEvaluator(meter)
                .getLastChecked(meter, channel.getMainReadingType())
                .orElse(Instant.MIN);
    }
}
