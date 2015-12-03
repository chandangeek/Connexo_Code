package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public Integer nbrOfFractionDigits;
    public long loadProfileId;
    public long version;
    public VersionInfo<String> parent;
    public BigDecimal multiplier;

    // optionally filled if requesting details
    public DetailedValidationInfo validationInfo;

    public static ChannelInfo from(Channel channel) {
        ChannelInfo info = new ChannelInfo();
        info.id = channel.getId();
        info.name = channel.getName();
        info.interval = new TimeDurationInfo(channel.getInterval());
        info.lastReading = channel.getLastReading().orElse(null);
        info.lastValueTimestamp = channel.getLastDateTime().orElse(null);
        info.readingType = new ReadingTypeInfo(channel.getReadingType());
        Optional<ReadingType> calculatedReadingType = channel.getCalculatedReadingType();
        if(calculatedReadingType.isPresent()){
            info.calculatedReadingType = new ReadingTypeInfo(calculatedReadingType.get());
        }
        info.overflowValue = channel.getOverflow();
        info.obisCode = channel.getObisCode();
        info.nbrOfFractionDigits = channel.getChannelSpec().getNbrOfFractionDigits();
        info.loadProfileId = channel.getLoadProfile().getId();
        info.version = channel.getLoadProfile().getVersion();
        Device device = channel.getDevice();
        BigDecimal multiplier = device.getMultiplier();
        if (multiplier.compareTo(BigDecimal.ONE) == 1) {
            info.multiplier = multiplier;
        }
        info.parent = new VersionInfo<>(device.getmRID(), device.getVersion());
        return info;
    }

    public static List<ChannelInfo> from(List<Channel> channels) {
        List<ChannelInfo> channelInfo = new ArrayList<>(channels.size());
        for (Channel channel : channels) {
            channelInfo.add(ChannelInfo.from(channel));
        }
        return channelInfo;
    }

    public static List<ChannelInfo> asSimpleInfoFrom(List<Channel> channels) {
        List<ChannelInfo> channelInfo = new ArrayList<>(channels.size());
        for (Channel channel : channels) {
            ChannelInfo info = new ChannelInfo();
            info.id = channel.getId();
            info.name = channel.getName();
            info.readingType = new ReadingTypeInfo(channel.getReadingType());
            channelInfo.add(info);
        }
        return channelInfo;
    }
}