package com.elster.insight.usagepoint.data.rest.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

//import com.energyict.mdc.common.ObisCode;
//import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.elster.insight.common.rest.TimeDurationInfo;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.TimeDuration.TimeUnit;

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

    // optionally filled if requesting details
    public DetailedValidationInfo validationInfo;

    public static ChannelInfo from(Channel channel) {
        ChannelInfo info = new ChannelInfo();
        info.id = channel.getId();
        
        info.interval = new TimeDurationInfo(new TimeDuration(channel.getMainReadingType().getMeasuringPeriod().getMinutes(), TimeUnit.MINUTES));
        info.unitOfMeasure = channel.getMainReadingType().getUnit().toString();
        
        
//        info.name = channel.getName();
//        info.interval = new TimeDurationInfo(channel.getInterval());
        
        info.unitOfMeasure = channel.getMainReadingType().getUnit().toString();
        
//        info.lastReading = channel.getLastReading().orElse(null);
        info.lastValueTimestamp = channel.getLastDateTime();
        
        info.readingType = new ReadingTypeInfo(channel.getMainReadingType());
        if (channel.getMainReadingType().isCumulative()) {
            channel.getMainReadingType().getCalculatedReadingType().ifPresent(
                    rt -> info.calculatedReadingType = new ReadingTypeInfo(rt));
        }
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
            info.readingType = new ReadingTypeInfo(channel.getMainReadingType());
            channelInfo.add(info);
        }
        return channelInfo;
    }
}