package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.data.Channel;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON representation of a channel
 * Created by bvn on 8/6/14.
 */
public class ChannelInfo {
    public long id;
    public String name;
    public TimeDurationInfo interval;
    public String unitOfMeasure;
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

    // optionally filled if requesting details
    public DetailedValidationInfo validationInfo;

    public static ChannelInfo from(Channel channel) {
        ChannelInfo info = new ChannelInfo();
        info.id = channel.getId();
        info.name = channel.getName();
        info.interval = new TimeDurationInfo(channel.getInterval());
        info.unitOfMeasure = channel.getUnit().toString();
        info.lastReading = channel.getLastReading().orElse(null);
        info.lastValueTimestamp = channel.getLastDateTime().orElse(null);
        info.readingType = new ReadingTypeInfo(channel.getReadingType());
        if (channel.getReadingType().isCumulative()) {
            channel.getReadingType().getCalculatedReadingType().ifPresent(
                    rt -> info.calculatedReadingType = new ReadingTypeInfo(rt));
        }
        info.overflowValue = channel.getOverflow();
        info.flowUnit = channel.getUnit().isFlowUnit() ? "flow" : "volume";
        info.obisCode = channel.getObisCode();
        info.nbrOfFractionDigits = channel.getChannelSpec().getNbrOfFractionDigits();
        info.loadProfileId = channel.getLoadProfile().getId();
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