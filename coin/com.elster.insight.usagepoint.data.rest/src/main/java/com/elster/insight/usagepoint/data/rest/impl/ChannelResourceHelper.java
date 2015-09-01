package com.elster.insight.usagepoint.data.rest.impl;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import com.elster.insight.common.services.ListPager;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.google.common.collect.Range;

public class ChannelResourceHelper {

    private static final Comparator<Channel> CHANNEL_COMPARATOR_BY_NAME = new ChannelComparator();

    private final ResourceHelper resourceHelper;
    private final Clock clock;
    private final ValidationInfoFactory validationInfoFactory;

    @Inject
    public ChannelResourceHelper(ResourceHelper resourceHelper, Clock clock, ValidationInfoFactory validationInfoFactory) {
        this.resourceHelper = resourceHelper;
        this.clock = clock;
        this.validationInfoFactory = validationInfoFactory;
    }

    public Response getChannels(String mrid, JsonQueryParameters queryParameters) {
        List<Channel> regularChannels = new ArrayList<Channel>();

        UsagePoint usagepoint = resourceHelper.findUsagePointByMrIdOrThrowException(mrid);

        Optional<? extends MeterActivation> currentActivation = usagepoint.getCurrentMeterActivation();
        if (currentActivation.isPresent()) {
            List<Channel> channelCandidates = currentActivation.get().getChannels();
            for (Channel channel : channelCandidates) {
                if (channel.isRegular())
                    regularChannels.add(channel);
            }

        } else {

            //TODO: no activation, throw exception?
            return null;
        }

        List<Channel> channelsPage = ListPager.of(regularChannels, CHANNEL_COMPARATOR_BY_NAME).from(queryParameters).find();

        //        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        //        List<Channel> channelsPage = ListPager.of(channelsProvider.apply(device), CHANNEL_COMPARATOR_BY_NAME).from(queryParameters).find();
        //
        List<ChannelInfo> channelInfos = new ArrayList<>();
        for (Channel channel : channelsPage) {
            ChannelInfo channelInfo = ChannelInfo.from(channel);
            //            addValidationInfo(channel, channelInfo);
            channelInfos.add(channelInfo);
        }
        return Response.ok(PagedInfoList.fromPagedList("channels", channelInfos, queryParameters)).build();
    }

    public Channel findCurrentChannelOnUsagePoint(String mrid, String rt_mrid) {
        UsagePoint usagepoint = resourceHelper.findUsagePointByMrIdOrThrowException(mrid);
        ChannelInfo channelInfo = null;
        Optional<? extends MeterActivation> currentActivation = usagepoint.getCurrentMeterActivation();
        if (currentActivation.isPresent()) {
            List<Channel> channelCandidates = currentActivation.get().getChannels();
            for (Channel channel : channelCandidates) {
                if (rt_mrid.equals(channel.getMainReadingType().getMRID())) {
                    return channel;
                }
            }

        } else {

            //TODO: no activation, throw exception?
            return null;
        }

        return null;

    }

//    public List<Channel> findChannelsForRange(String mrid, String rt_mrid, Range<Instant> range) {
//        UsagePoint usagepoint = resourceHelper.findUsagePointByMrIdOrThrowException(mrid);
//        return usagepoint.getMeterActivations().stream()
//                .filter(meterActivation -> !meterActivation.getRange().intersection(range).isEmpty())
//                .map(MeterActivation::getChannels)
//                .flatMap(channels -> channels.stream().filter(channel -> channel.getMainReadingType().getMRID().equals(rt_mrid)))
//                .collect(Collectors.toList());
//    }

    public Response getChannel(Supplier<Channel> channelSupplier) {
        Channel channel = channelSupplier.get();
        ChannelInfo channelInfo = ChannelInfo.from(channel);
        //        addValidationInfo(channel, channelInfo);
        return Response.ok(channelInfo).build();
    }

    //    public void addValidationInfo(Channel channel, ChannelInfo channelInfo) {
    //        List<DataValidationStatus> states =
    //                channel.getDevice().forValidation().getValidationStatus(channel, Collections.emptyList(), lastMonth());
    //        channelInfo.validationInfo = validationInfoFactory.createDetailedValidationInfo(isValidationActive(channel), states, channel.getDevice().forValidation().getLastChecked(channel));
    //        if (states.isEmpty()) {
    //            channelInfo.validationInfo.dataValidated = channel.getDevice().forValidation().allDataValidated(channel, clock.instant());
    //        }
    //    }

    //    public boolean isValidationActive(Channel channel) {
    //        return channel.getDevice().forValidation().isValidationActive(channel, clock.instant());
    //    }

    private Range<Instant> lastMonth() {
        ZonedDateTime end = clock.instant().atZone(ZoneId.systemDefault()).with(ChronoField.MILLI_OF_DAY, 0L).plusDays(1);
        ZonedDateTime start = end.minusMonths(1);
        return Range.openClosed(start.toInstant(), end.toInstant());
    }

    private boolean hasData(Channel channel) {
        return channel.hasData();
    }

    //    public ValidationStatusInfo determineStatus(Channel channel) {
    //        return new ValidationStatusInfo(isValidationActive(channel), channel.getDevice().forValidation().getLastChecked(channel), hasData(channel));
    //    }

    //    public String getChannelName(Channel channel) {
    //        ReadingTypeInfo readingTypeInfo = new ReadingTypeInfo(channel.getMainReadingType());
    //        StringBuilder channelReadingTypeName = new StringBuilder();
    //        channelReadingTypeName.append(readingTypeInfo.aliasName);
    //        if (!readingTypeInfo.names.timeOfUse.isEmpty()) {
    //            channelReadingTypeName.append(' ').append(readingTypeInfo.names.timeOfUse);
    //        }
    //        channelReadingTypeName.append(' ').append('(').append(readingTypeInfo.names.unitOfMeasure).append(')');
    //        return channelReadingTypeName.toString();
    //    }

}