/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.DataLoggerChannelUsage;
import com.energyict.mdc.device.topology.TopologyService;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ChannelInfoFactory {

    private final Clock clock;
    private final TopologyService topologyService;
    private final ReadingTypeInfoFactory readingTypeInfoFactory;

    @Inject
    public ChannelInfoFactory(Clock clock,
                              TopologyService topologyService,
                              ReadingTypeInfoFactory readingTypeInfoFactory) {
        this.clock = clock;
        this.topologyService = topologyService;
        this.readingTypeInfoFactory = readingTypeInfoFactory;
    }

    public ChannelInfo from(Channel channel) {
        ChannelInfo info = new ChannelInfo();
        info.id = channel.getId();
        info.name = channel.getName();
        info.interval = new TimeDurationInfo(channel.getInterval());
        info.lastReading = channel.getLastReading().orElse(null);
        Optional<Channel> slaveChannel = topologyService.getSlaveChannel(channel, clock.instant());
        if (!slaveChannel.isPresent()) {
            info.lastValueTimestamp = channel.getLastDateTime().orElse(null);
        } else {
            info.lastValueTimestamp = slaveChannel.get().getLastDateTime().orElse(null);
        }
        info.readingType = readingTypeInfoFactory.from(channel.getReadingType());
        channel.getCalculatedReadingType(clock.instant()).ifPresent(readingType1 -> info.calculatedReadingType = readingTypeInfoFactory.from(readingType1));
        channel.getChannelSpec().getOverflow().ifPresent(channelSpecOverflow -> info.overflowValue = channelSpecOverflow);
        channel.getOverflow().ifPresent(overruledOverflowValue -> info.overruledOverflowValue = overruledOverflowValue);
        info.flowUnit = channel.getUnit().isFlowUnit() ? "flow" : "volume";
        info.obisCode = channel.getChannelSpec().getDeviceObisCode();
        info.overruledObisCode = channel.getObisCode();
        info.nbrOfFractionDigits = channel.getChannelSpec().getNbrOfFractionDigits();
        info.overruledNbrOfFractionDigits = channel.getNrOfFractionDigits();
        info.loadProfileId = channel.getLoadProfile().getId();
        info.loadProfileName = channel.getLoadProfile().getLoadProfileSpec().getLoadProfileType().getName();
        info.version = channel.getLoadProfile().getVersion();
        Device device = channel.getDevice();
        info.useMultiplier = channel.getChannelSpec().isUseMultiplier();
        info.multiplier = channel.getMultiplier(clock.instant()).orElseGet(() -> null);
        info.parent = new VersionInfo<>(device.getName(), device.getVersion());
        List<DataLoggerChannelUsage> dataLoggerChannelUsages = topologyService.findDataLoggerChannelUsagesForChannels(channel, Range.atLeast(clock.instant()));
        if (!dataLoggerChannelUsages.isEmpty()) {
            info.dataloggerSlaveName = dataLoggerChannelUsages.get(0).getDataLoggerReference().getOrigin().getName();
        }
        return info;
    }

    public List<ChannelInfo> from(List<Channel> channels) {
        return channels.stream().map(this::from).collect(Collectors.toList());
    }

    public List<ChannelInfo> asSimpleInfoFrom(List<Channel> channels) {
        return channels.stream().map(simpleChannel -> {
            ChannelInfo info = new ChannelInfo();
            info.id = simpleChannel.getId();
            info.name = simpleChannel.getName();
            info.readingType = readingTypeInfoFactory.from(simpleChannel.getReadingType());
            info.lastValueTimestamp = simpleChannel.getLastDateTime().orElse(null);
            return info;
        }).collect(Collectors.toList());
    }
}
