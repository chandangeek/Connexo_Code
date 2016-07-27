package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.FullySpecifiedReadingTypeRequirement;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.ReadingTypeRequirement;
import com.elster.jupiter.metering.config.ReadingTypeRequirementChecker;
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

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
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
    private final ExceptionFactory exceptionFactory;

    @Inject
    public UsagePointResource(MeterInfoFactory meterInfoFactory,
                              UsagePointChannelInfoFactory usagePointChannelInfoFactory,
                              ResourceHelper resourceHelper,
                              ChannelDataInfoFactory channelDataInfoFactory,
                              ValidationService validationService,
                              ExceptionFactory exceptionFactory) {
        this.meterInfoFactory = meterInfoFactory;
        this.usagePointChannelInfoFactory = usagePointChannelInfoFactory;
        this.resourceHelper = resourceHelper;
        this.channelDataInfoFactory = channelDataInfoFactory;
        this.validationService = validationService;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Path("/{mRID}/history/devices")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT, Privileges.Constants.ADMINISTER_OWN_USAGEPOINT})
    public PagedInfoList getDevicesHistory(@PathParam("mRID") String mRID, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointOrThrowException(mRID);
        return PagedInfoList.fromCompleteList("devices", meterInfoFactory.getDevicesHistory(usagePoint), queryParameters);
    }

    @GET
    @Path("/{mRID}/channels")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT, Privileges.Constants.ADMINISTER_OWN_USAGEPOINT})
    public PagedInfoList getChannels(@PathParam("mRID") String mRID, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointOrThrowException(mRID);
        Optional<EffectiveMetrologyConfigurationOnUsagePoint> effectiveMetrologyConfiguration = usagePoint.getEffectiveMetrologyConfiguration();
        List<UsagePointChannelInfo> channelInfos = Collections.emptyList();
        if (effectiveMetrologyConfiguration.isPresent()) {
            EffectiveMetrologyConfigurationOnUsagePoint effectiveMC = effectiveMetrologyConfiguration.get();
            UsagePointMetrologyConfiguration metrologyConfiguration = effectiveMC.getMetrologyConfiguration();
            channelInfos = metrologyConfiguration.getContracts().stream()
                    .map(effectiveMC::getChannelsContainer)
                    .flatMap(Functions.asStream())
                    .flatMap(channelsContainer -> channelsContainer.getChannels().stream())
                    .map(channel -> usagePointChannelInfoFactory.from(channel, usagePoint, metrologyConfiguration))
                    .collect(Collectors.toList());
        }
        return PagedInfoList.fromPagedList("channels", channelInfos, queryParameters);
    }

    @GET
    @Path("/{mRID}/channels/{channelId}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT, Privileges.Constants.ADMINISTER_OWN_USAGEPOINT})
    public UsagePointChannelInfo getChannel(@PathParam("mRID") String mRID, @PathParam("channelId") long channelId) {
        UsagePoint usagePoint = resourceHelper.findUsagePointOrThrowException(mRID);
        EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration = usagePoint.getEffectiveMetrologyConfiguration()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_METROLOGY_CONFIG_FOR_USAGE_POINT, usagePoint.getMRID()));
        Channel channel = resourceHelper.findChannelOnUsagePointOrThrowException(effectiveMetrologyConfiguration, channelId);
        return usagePointChannelInfoFactory.from(channel, usagePoint, effectiveMetrologyConfiguration.getMetrologyConfiguration());
    }

    @GET
    @Path("/{mRID}/channels/{channelId}/data")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT, Privileges.Constants.ADMINISTER_ANY_USAGEPOINT, Privileges.Constants.ADMINISTER_OWN_USAGEPOINT})
    public PagedInfoList getChannelData(@PathParam("mRID") String mRID, @PathParam("channelId") long channelId, @BeanParam JsonQueryFilter filter, @BeanParam JsonQueryParameters queryParameters) {
        UsagePoint usagePoint = resourceHelper.findUsagePointOrThrowException(mRID);
        List<ChannelDataInfo> outputChannelDataInfoList = Collections.emptyList();
        if (filter.hasProperty("intervalStart") && filter.hasProperty("intervalEnd")) {
            Range<Instant> requestedInterval = Ranges.openClosed(filter.getInstant("intervalStart"), filter.getInstant("intervalEnd"));
            EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration = usagePoint.getEffectiveMetrologyConfiguration()
                    .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_METROLOGY_CONFIG_FOR_USAGE_POINT, usagePoint.getMRID()));
            Channel channel = resourceHelper.findChannelOnUsagePointOrThrowException(effectiveMetrologyConfiguration, channelId);
            ChannelsContainer channelsContainer = channel.getChannelsContainer();
            if (channelsContainer.getRange().isConnected(requestedInterval)) {
                Range<Instant> effectiveInterval = channelsContainer.getRange().intersection(requestedInterval);
                TemporalAmount intervalLength = channel.getIntervalLength().get();
                Map<Instant, IntervalReadingWithValidationStatus> preFilledChannelDataMap = channel.toList(effectiveInterval).stream()
                        .collect(Collectors.toMap(Function.identity(), timeStamp -> new IntervalReadingWithValidationStatus(timeStamp, intervalLength)));

                // add readings to pre filled channel data map
                List<IntervalReadingRecord> intervalReadings = channel.getIntervalReadings(effectiveInterval);
                for (IntervalReadingRecord intervalReadingRecord : intervalReadings) {
                    IntervalReadingWithValidationStatus readingHolder = preFilledChannelDataMap.get(intervalReadingRecord.getTimeStamp());
                    readingHolder.setIntervalReadingRecord(intervalReadingRecord);
                }
                Map<Range<Instant>, Instant> lastCheckedOfSourceChannels = getLastCheckedOfSourceChannels(effectiveMetrologyConfiguration, channel);
                outputChannelDataInfoList = preFilledChannelDataMap.entrySet().stream()
                        .sorted(Collections.reverseOrder(Comparator.comparing(Map.Entry::getKey)))
                        .map(Map.Entry::getValue)
                        .map(reading -> channelDataInfoFactory.asInfo(reading, lastCheckedOfSourceChannels))
                        .collect(Collectors.toList());
            }
        }
        return PagedInfoList.fromCompleteList("data", outputChannelDataInfoList, queryParameters);
    }

    private Map<Range<Instant>, Instant> getLastCheckedOfSourceChannels(EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration, Channel aggregatedChannel) {
        Optional<ReadingTypeRequirement> readingTypeRequirement = getReadingTypeRequirement(effectiveMetrologyConfiguration, aggregatedChannel);
        if (readingTypeRequirement.isPresent()) {
            return findSourceChannelsOnUsagePoint(readingTypeRequirement.get(), effectiveMetrologyConfiguration.getUsagePoint()).stream()
                    .collect(Collectors.toMap(this::getRange, this::getLastChecked));
        } else {
            return Collections.emptyMap();
        }
    }

    private Optional<ReadingTypeRequirement> getReadingTypeRequirement(EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration, Channel channel) {
        MetrologyContract contract = resourceHelper.findMetrologyContractOfChannelOrThrowException(effectiveMetrologyConfiguration, channel.getId());
        Optional<ReadingTypeDeliverable> readingTypeDeliverable = contract.getDeliverables().stream()
                .filter(deliverable -> deliverable.getReadingType().equals(channel.getMainReadingType()))
                .findFirst();
        ReadingTypeRequirementChecker readingTypeRequirementChecker = new ReadingTypeRequirementChecker();
        readingTypeDeliverable.get().getFormula().getExpressionNode().accept(readingTypeRequirementChecker);
        List<ReadingTypeRequirement> readingTypeRequirements = readingTypeRequirementChecker.getReadingTypeRequirements();
        if (!readingTypeRequirements.isEmpty()) {
            FullySpecifiedReadingTypeRequirement readingTypeRequirement = (FullySpecifiedReadingTypeRequirement) readingTypeRequirements.get(0);// expecting max 1 requirement
            return Optional.of(readingTypeRequirement);
        }
        return Optional.empty();
    }

    private List<Channel> findSourceChannelsOnUsagePoint(ReadingTypeRequirement readingTypeRequirement, UsagePoint usagePoint) {
        return usagePoint.getMeterActivations().stream()
                .map(meterActivation -> findSourceChannelOnMeterActivation(readingTypeRequirement, meterActivation))
                .flatMap(Functions.asStream())
                .collect(Collectors.toList());
    }

    private Optional<Channel> findSourceChannelOnMeterActivation(ReadingTypeRequirement readingTypeRequirement, MeterActivation meterActivation) {
        return meterActivation.getChannelsContainer().getChannels().stream()
                .filter(channel -> channel.getReadingTypes().stream().anyMatch(readingTypeRequirement::matches))
                .findAny();
    }

    private Range<Instant> getRange(Channel channel) {
        return channel.getChannelsContainer().getRange();
    }

    private Instant getLastChecked(Channel channel) {
        ChannelsContainer channelsContainer = channel.getChannelsContainer();
        Instant startTime = channelsContainer.getStart();
        Meter meter = channelsContainer.getMeter(startTime).get();
        return validationService.getEvaluator(meter, Range.all()).getLastChecked(meter, channel.getMainReadingType()).orElse(Instant.MIN);
    }
}
