package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;

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
    public BigDecimal overflowValue;
    public String flowUnit;
    public Integer nbrOfFractionDigits;
    public long loadProfileId;
    public long version;
    public VersionInfo<String> parent;
    public BigDecimal multiplier;

    // optionally filled if requesting details
    public DetailedValidationInfo validationInfo;

    public static ChannelInfo from(Channel channel, Clock clock) {
        ChannelInfo info = new ChannelInfo();
        info.id = channel.getId();
        info.name = channel.getName();
        info.interval = new TimeDurationInfo(channel.getInterval());
        info.lastReading = channel.getLastReading().orElse(null);
        info.lastValueTimestamp = channel.getLastDateTime().orElse(null);
        info.readingType = new ReadingTypeInfo(channel.getReadingType());
        channel.getCalculatedReadingType(clock.instant()).ifPresent(readingType1 -> info.calculatedReadingType = new ReadingTypeInfo(readingType1));
        channel.getOverflow().ifPresent(overflow -> info.overflowValue = overflow);
        info.flowUnit = channel.getUnit().isFlowUnit() ? "flow" : "volume";
        info.obisCode = channel.getObisCode();
        info.nbrOfFractionDigits = channel.getChannelSpec().getNbrOfFractionDigits();
        info.loadProfileId = channel.getLoadProfile().getId();
        info.version = channel.getLoadProfile().getVersion();
        Device device = channel.getDevice();
        info.multiplier = channel.getMultiplier(clock.instant()).orElseGet(() -> null);
        info.parent = new VersionInfo<>(device.getmRID(), device.getVersion());
        return info;
    }

    public static List<ChannelInfo> from(List<Channel> channels, Clock clock) {
        return  channels.stream().map(channel -> from(channel, clock)).collect(Collectors.toList());
    }

    public static List<ChannelInfo> asSimpleInfoFrom(List<Channel> channels) {
        return channels.stream().map(simpleChannel -> {
            ChannelInfo info = new ChannelInfo();
            info.id = simpleChannel.getId();
            info.name = simpleChannel.getName();
            info.readingType = new ReadingTypeInfo(simpleChannel.getReadingType());
            return info;
        }).collect(Collectors.toList());
    }
}