package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
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
import java.util.function.Function;
import java.util.function.Supplier;

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

    public Response getChannels(String mrid, Function<Device, List<Channel>> channelsProvider, JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        List<Channel> channelsPage = ListPager.of(channelsProvider.apply(device), CHANNEL_COMPARATOR_BY_NAME).from(queryParameters).find();

        List<ChannelInfo> channelInfos = new ArrayList<>();
        for (Channel channel : channelsPage) {
            ChannelInfo channelInfo = ChannelInfo.from(channel);
            addValidationInfo(channel, channelInfo);
            channelInfos.add(channelInfo);
        }
        return Response.ok(PagedInfoList.fromPagedList("channels", channelInfos, queryParameters)).build();
    }

    public Response getChannel(Supplier<Channel> channelSupplier) {
        Channel channel = channelSupplier.get();
        ChannelInfo channelInfo = ChannelInfo.from(channel);
        addValidationInfo(channel, channelInfo);
        return Response.ok(channelInfo).build();
    }

    public Response getChannelValidationInfo(Supplier<Channel> channelSupplier) {
        Channel channel = channelSupplier.get();
        ChannelInfo channelInfo = ChannelInfo.from(channel);
        addValidationInfo(channel, channelInfo);
        return Response.ok(channelInfo.validationInfo).build();
    }

    public void addValidationInfo(Channel channel, ChannelInfo channelInfo) {
        List<DataValidationStatus> states =
                channel.getDevice().forValidation().getValidationStatus(channel, Collections.emptyList(), lastMonth());
        channelInfo.validationInfo = validationInfoFactory.createDetailedValidationInfo(isValidationActive(channel), states, channel.getDevice().forValidation().getLastChecked(channel));
        if (states.isEmpty()) {
            channelInfo.validationInfo.dataValidated = channel.getDevice().forValidation().allDataValidated(channel, clock.instant());
        }
    }

    public boolean isValidationActive(Channel channel) {
        return channel.getDevice().forValidation().isValidationActive(channel, clock.instant());
    }

    private Range<Instant> lastMonth() {
        ZonedDateTime end = clock.instant().atZone(ZoneId.systemDefault()).with(ChronoField.MILLI_OF_DAY, 0L).plusDays(1);
        ZonedDateTime start = end.minusMonths(1);
        return Range.openClosed(start.toInstant(), end.toInstant());
    }

    private boolean hasData(Channel channel) {
        return channel.hasData();
    }

    public ValidationStatusInfo determineStatus(Channel channel) {
        return new ValidationStatusInfo(isValidationActive(channel), channel.getDevice().forValidation().getLastChecked(channel), hasData(channel));
    }

    public String getChannelName(Channel channel) {
        ReadingTypeInfo readingTypeInfo = new ReadingTypeInfo(channel.getReadingType());
        StringBuilder channelReadingTypeName = new StringBuilder();
        channelReadingTypeName.append(readingTypeInfo.aliasName);
        if (!readingTypeInfo.names.timeOfUse.isEmpty()) {
            channelReadingTypeName.append(' ').append(readingTypeInfo.names.timeOfUse);
        }
        channelReadingTypeName.append(' ').append('(').append(readingTypeInfo.names.unitOfMeasure).append(')');
        return channelReadingTypeName.toString();
    }

}