package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.validation.DataValidationStatus;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
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
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class ChannelResourceHelper {
    private static final Comparator<Channel> CHANNEL_COMPARATOR_BY_NAME = new ChannelComparator();

    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final Thesaurus thesaurus;
    private final Clock clock;

    @Inject
    public ChannelResourceHelper(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory, Thesaurus thesaurus, Clock clock) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.thesaurus = thesaurus;
        this.clock = clock;
    }

    public Response getChannels(String mrid, Function<Device, List<Channel>> channelsProvider, QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        List<Channel> channelsPage = ListPager.of(channelsProvider.apply(device), CHANNEL_COMPARATOR_BY_NAME).from(queryParameters).find();

        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
        for (Channel channel : channelsPage) {
            ChannelInfo channelInfo = ChannelInfo.from(channel);
            addValidationInfo(channel, channelInfo);
            channelInfos.add(channelInfo);
        }
        return Response.ok(PagedInfoList.asJson("channels", channelInfos, queryParameters)).build();
    }

    public Response getChannel(Supplier<Channel> channelSupplier) {
        Channel channel = channelSupplier.get();
        ChannelInfo channelInfo = ChannelInfo.from(channel);
        addValidationInfo(channel, channelInfo);
        return Response.ok(channelInfo).build();
    }

    public void addValidationInfo(Channel channel, ChannelInfo channelInfo) {
        List<DataValidationStatus> states =
                channel.getDevice().forValidation().getValidationStatus(channel, Collections.emptyList(), lastMonth());
        channelInfo.validationInfo = new DetailedValidationInfo(isValidationActive(channel), states, lastChecked(channel));
        if (states.isEmpty()) {
            channelInfo.validationInfo.dataValidated = channel.getDevice().forValidation().allDataValidated(channel, clock.instant());
        }
    }

    public boolean isValidationActive(Channel channel) {
        return channel.getDevice().forValidation().isValidationActive(channel, clock.instant());
    }

    private Date lastChecked(Channel channel) {
        Optional<Instant> optional = channel.getDevice().forValidation().getLastChecked(channel);
        return optional.map(Date::from).orElse(null);
    }

    private Range<Instant> lastMonth() {
        ZonedDateTime end = clock.instant().atZone(ZoneId.systemDefault()).with(ChronoField.MILLI_OF_DAY, 0L).plusDays(1);
        ZonedDateTime start = end.minusMonths(1);
        return Range.openClosed(start.toInstant(), end.toInstant());
    }

    private boolean hasData(Channel channel) {
        return channel.getDevice().forValidation().hasData(channel);
    }

    public ValidationStatusInfo determineStatus(Channel channel) {
        return new ValidationStatusInfo(isValidationActive(channel), lastChecked(channel), hasData(channel));
    }
}
