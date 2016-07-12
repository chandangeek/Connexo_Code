package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.DataLoggerChannelUsage;
import com.energyict.mdc.device.topology.TopologyService;

import com.google.common.collect.Range;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JSON representation of a channel
 * Created by bvn on 8/6/14.
 */
public class ChannelInfo {
    public long id;
    public String name;
    public TimeDurationInfo interval;
    public Instant lastReading;
    public Instant lastValueTimestamp;
    public ReadingTypeInfo readingType;
    public ReadingTypeInfo calculatedReadingType;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obisCode;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode overruledObisCode;
    public BigDecimal overflowValue;
    public BigDecimal overruledOverflowValue;
    public String flowUnit;
    public Integer nbrOfFractionDigits;
    public Integer overruledNbrOfFractionDigits;
    public long loadProfileId;
    public String loadProfileName;
    public long version;
    public VersionInfo<String> parent;
    public Boolean useMultiplier;
    public BigDecimal multiplier;
    public String dataloggerSlavemRID;

    // optionally filled if requesting details
    public DetailedValidationInfo validationInfo;

    public static ChannelInfo from(Channel channel, Clock clock, TopologyService topologyService) {
        ChannelInfo info = new ChannelInfo();
        info.id = channel.getId();
        info.name = channel.getName();
        info.interval = new TimeDurationInfo(channel.getInterval());
        info.lastReading = channel.getLastReading().orElse(null);
        info.lastValueTimestamp = channel.getLastDateTime().orElse(null);
        info.readingType = new ReadingTypeInfo(channel.getReadingType());
        channel.getCalculatedReadingType(clock.instant()).ifPresent(readingType1 -> info.calculatedReadingType = new ReadingTypeInfo(readingType1));
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
        info.parent = new VersionInfo<>(device.getmRID(), device.getVersion());
        List<DataLoggerChannelUsage> dataLoggerChannelUsages = topologyService.findDataLoggerChannelUsagesForChannels(channel, Range.atLeast(clock.instant()));
        if (!dataLoggerChannelUsages.isEmpty()) {
            info.dataloggerSlavemRID = dataLoggerChannelUsages.get(0).getDataLoggerReference().getOrigin().getmRID();
        }
        return info;
    }

    public static List<ChannelInfo> from(List<Channel> channels, Clock clock, TopologyService topologyService) {
        return channels.stream().map(channel -> from(channel, clock, topologyService)).collect(Collectors.toList());
    }

    public static List<ChannelInfo> asSimpleInfoFrom(List<Channel> channels) {
        return channels.stream().map(simpleChannel -> {
            ChannelInfo info = new ChannelInfo();
            info.id = simpleChannel.getId();
            info.name = simpleChannel.getName();
            info.readingType = new ReadingTypeInfo(simpleChannel.getReadingType());
            info.lastValueTimestamp = simpleChannel.getLastDateTime().orElse(null);
            return info;
        }).collect(Collectors.toList());
    }
}