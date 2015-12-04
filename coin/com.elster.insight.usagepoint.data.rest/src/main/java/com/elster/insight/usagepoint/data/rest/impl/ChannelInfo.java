package com.elster.insight.usagepoint.data.rest.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

//import com.energyict.mdc.common.ObisCode;
//import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.elster.insight.common.rest.TimeDurationInfo;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.time.TimeDuration.TimeUnit;

/**
 * JSON representation of a channel
 */
public class ChannelInfo {
    public long id;
    public String name;
    public TimeDurationInfo interval;
    public String unitOfMeasure;
    public BigDecimal lastReadingValue;
    public Instant lastValueTimestamp;
    public ReadingTypeInfo readingType;
    public ReadingTypeInfo calculatedReadingType;
  
    public String flowUnit;
    public long version;
    // optionally filled if requesting details
    public DetailedValidationInfo validationInfo;

    public static ChannelInfo from(Channel channel) {
        ChannelInfo info = new ChannelInfo();
        info.id = channel.getId();
        info.name = channel.getMainReadingType().getAliasName();
        
        info.interval = new TimeDurationInfo(new TimeDuration(channel.getMainReadingType().getMeasuringPeriod().getMinutes(), TimeUnit.MINUTES));
        
        info.unitOfMeasure = channel.getMainReadingType().getMultiplier().getSymbol() + channel.getMainReadingType().getUnit().getSymbol();
        
        info.lastReadingValue = channel.getReading(channel.getLastDateTime()).get().getValue();
        info.lastValueTimestamp = channel.getLastDateTime();
        
        info.readingType = new ReadingTypeInfo(channel.getMainReadingType());
        if (channel.getMainReadingType().isCumulative()) {
            channel.getMainReadingType().getCalculatedReadingType().ifPresent(
                    rt -> info.calculatedReadingType = new ReadingTypeInfo(rt));
        }
        info.flowUnit = isFlowUnit(channel.getMainReadingType().getUnit()) ? "flow" : "volume";
        channel.getMeterActivation().getUsagePoint().ifPresent(up -> info.version = up.getVersion());
        return info;
    }

    private static boolean isFlowUnit(ReadingTypeUnit unit) {
        switch (unit) {
            case WATTHOUR:
            case AMPEREHOUR:
            case CUBICFEET:
            case VOLTAMPEREHOUR:
            case VOLTAMPEREREACTIVEHOUR:
            case IMPERIALGALLON:
            case CUBICFEETCOMPENSATED:
            case CUBICFEETUNCOMPENSATED:
            case CUBICMETER:
            case CUBICMETERCOMPENSATED:
            case CUBICMETERUNCOMPENSATED:
            case CUBICYARD:
            case LITRE:
            case LITRECOMPENSATED:
            case LITREUNCOMPENSATED:
            case USGALLON:
            case VOLTHOUR:
                return true;
            default: 
                return false;
        }
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