package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.ListPager;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class ChannelResourceHelper {

    private static final Comparator<Channel> CHANNEL_COMPARATOR_BY_NAME = new ChannelComparator();

    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final Clock clock;
    private final Thesaurus thesaurus;
    private final ValidationService validationService;
    private final UsagePointConfigurationService usagePointConfigurationService;
    private final ValidationInfoFactory validationInfoFactory;

    @Inject
    public ChannelResourceHelper(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory, Clock clock, Thesaurus thesaurus, ValidationService validationService, UsagePointConfigurationService usagePointConfigurationService, ValidationInfoFactory validationInfoFactory) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.clock = clock;
        this.thesaurus = thesaurus;
        this.validationService = validationService;
        this.usagePointConfigurationService = usagePointConfigurationService;
        this.validationInfoFactory = validationInfoFactory;
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
        List<Channel> channels = ListPager.of(regularChannels, CHANNEL_COMPARATOR_BY_NAME).from(queryParameters).find();
        //.stream().map(ChannelInfo::from).collect(Collectors.toList());
        
        List<ChannelInfo> channelInfos = new ArrayList<>();
        for (Channel channel : channels) {
            ChannelInfo channelInfo = ChannelInfo.from(channel);
            addValidationInfo(channel, channelInfo, usagepoint);
            channelInfos.add(channelInfo);
        }
        
        return Response.ok(PagedInfoList.fromPagedList("channels", channelInfos, queryParameters)).build();
    }

    public Optional<Channel> findCurrentChannelOnUsagePoint(String mrid, String rt_mrid) {
        UsagePoint usagepoint = resourceHelper.findUsagePointByMrIdOrThrowException(mrid);
        MeterActivation currentActivation = usagepoint.getCurrentMeterActivation().orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_CURRENT_ACTIVATION_FOR_USAGE_POINT_FOR_MRID, mrid));
        return currentActivation.getChannels().stream()
                .filter(channel->rt_mrid.equals(channel.getMainReadingType().getMRID())).findFirst();
    }

    public Response getChannel(Supplier<Channel> channelSupplier, UsagePoint usagepoint) {
        Channel channel = channelSupplier.get();
        ChannelInfo channelInfo = ChannelInfo.from(channel);
        addValidationInfo(channel, channelInfo, usagepoint);
        return Response.ok(channelInfo).build();
    }
    
    public void addValidationInfo(Channel channel, ChannelInfo channelInfo, UsagePoint usagepoint) {
        UsagePointValidation upv = getUsagePointValidation(usagepoint);
        List<DataValidationStatus> states =
                upv.getValidationStatus(channel, Collections.emptyList(), lastMonth());
        channelInfo.validationInfo = validationInfoFactory.createDetailedValidationInfo(isValidationActive(channel, upv), states, upv.getLastChecked(channel));
        if (states.isEmpty()) {
            channelInfo.validationInfo.dataValidated = upv.allDataValidated(channel, clock.instant());
        }
    }
    
    public boolean isValidationActive(Channel channel, UsagePointValidation upv) {
        return upv.isValidationActive(channel, clock.instant());
    }
    
    private Range<Instant> lastMonth() {
        ZonedDateTime end = clock.instant().atZone(ZoneId.systemDefault()).with(ChronoField.MILLI_OF_DAY, 0L).plusDays(1);
        ZonedDateTime start = end.minusMonths(1);
        return Range.openClosed(start.toInstant(), end.toInstant());
    }
    
    public UsagePointValidation getUsagePointValidation(UsagePoint usagePoint) {
        return new UsagePointValidationImpl(validationService, clock, thesaurus, usagePoint, usagePointConfigurationService);
    }

    public Optional<Channel> findChannel(UsagePoint usagepoint, Instant instant, String rt_mrid) {
        MeterActivation meterActivation = usagepoint.getMeterActivation(instant).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_ACTIVATION_FOR_USAGE_POINT_FOR_MRID_AT_TIME, usagepoint.getMRID(), instant));
        return meterActivation.getChannels().stream()
                .filter(channel->rt_mrid.equals(channel.getMainReadingType().getMRID())).findFirst();
    }
    
}
