/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.validation.DataValidationStatus;
import com.energyict.mdc.common.device.data.Channel;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.services.ListPager;

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
import java.util.function.Function;
import java.util.function.Supplier;

public class ChannelResourceHelper {

    private static final Comparator<Channel> CHANNEL_COMPARATOR_BY_NAME = new ChannelComparator();

    private final ResourceHelper resourceHelper;
    private final Clock clock;
    private final ValidationInfoFactory validationInfoFactory;
    private final ChannelInfoFactory channelInfoFactory;

    @Inject
    public ChannelResourceHelper(ResourceHelper resourceHelper,
                                 Clock clock,
                                 ValidationInfoFactory validationInfoFactory,
                                 ChannelInfoFactory channelInfoFactory) {
        this.resourceHelper = resourceHelper;
        this.clock = clock;
        this.validationInfoFactory = validationInfoFactory;
        this.channelInfoFactory = channelInfoFactory;
    }

    public Response getChannels(String deviceName, Function<Device, List<Channel>> channelsProvider, JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(deviceName);
        List<Channel> channelsPage = ListPager.of(channelsProvider.apply(device), CHANNEL_COMPARATOR_BY_NAME).from(queryParameters).find();

        List<ChannelInfo> channelInfos = new ArrayList<>();
        for (Channel channel : channelsPage) {
            ChannelInfo channelInfo = channelInfoFactory.from(channel);
            addValidationInfo(channel, channelInfo);
            channelInfos.add(channelInfo);
        }

        Collections.sort(channelInfos, this::compareChannelInfo);
        return Response.ok(PagedInfoList.fromPagedList("channels", channelInfos, queryParameters)).build();
    }

    private int compareChannelInfo(ChannelInfo ci1, ChannelInfo ci2) {
        return ci1.readingType.fullAliasName.compareTo(ci2.readingType.fullAliasName);
    }

    public Response getChannel(Supplier<Channel> channelSupplier) {
        Channel channel = channelSupplier.get();
        ChannelInfo channelInfo = channelInfoFactory.from(channel);
        addValidationInfo(channel, channelInfo);
        return Response.ok(channelInfo).build();
    }

    public Response getChannelValidationInfo(Supplier<Channel> channelSupplier) {
        Channel channel = channelSupplier.get();
        ChannelInfo channelInfo = channelInfoFactory.from(channel);
        addValidationInfo(channel, channelInfo);
        return Response.ok(channelInfo.validationInfo).build();
    }

    /**
     * Linked with {@link LoadProfileResource#allDataValidatedOnChannel(Channel, Range)}
     */
    public void addValidationInfo(Channel channel, ChannelInfo channelInfo) {
        List<DataValidationStatus> states =
                channel.getDevice().forValidation().getValidationStatus(channel, Collections.emptyList(), lastMonth());
        Optional<Instant> lastCheck  = Optional.empty();
        if(channel.getDevice().forValidation().getLastChecked(channel).isPresent()){
            lastCheck = channel.getDevice().forValidation().getLastChecked(channel).equals(Optional.of(channel.getDevice().getMeterActivationsMostRecentFirst().get(0).getStart())) ? Optional.empty() : channel.getDevice().forValidation().getLastChecked(channel);
        }
        channelInfo.validationInfo = validationInfoFactory.createDetailedValidationInfo(isValidationActive(channel), states, lastCheck);
        if (states.isEmpty()) {
            channelInfo.validationInfo.dataValidated = channel.getDevice().forValidation().allDataValidated(channel);
        }
        channelInfo.validationInfo.channelValidationStatus = isChannelValidationActive(channel);
    }

    public boolean isValidationActive(Channel channel) {
        return channel.getDevice().forValidation().isValidationActive();
    }

    private boolean isChannelValidationActive(Channel channel){
        return channel.getDevice().forValidation().isChannelStatusActive(channel);
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
        return new ValidationStatusInfo(isValidationActive(channel),
                channel.getDevice().forValidation().getLastChecked(channel).orElse(null),
                hasData(channel));
    }

}
