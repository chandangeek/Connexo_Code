package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.masterdata.rest.PhenomenonInfo;

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
    public PhenomenonInfo unitOfMeasure;
    public Instant lastReading;
    public ReadingTypeInfo readingType;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obisCode;
    public BigDecimal multiplier;
    public BigDecimal overflowValue;
    public String flowUnit;
    public Integer nbrOfFractionDigits;

    // optionally filled if requesting details
    public DetailedValidationInfo validationInfo;

    public static ChannelInfo from(Channel channel) {
        ChannelInfo info = new ChannelInfo();
        info.id = channel.getId();
        info.name = channel.getName();
        info.interval = new TimeDurationInfo(channel.getInterval());
        info.unitOfMeasure = PhenomenonInfo.from(channel.getPhenomenon());
        info.lastReading = channel.getLastReading().orElse(null);
        info.readingType = new ReadingTypeInfo(channel.getReadingType());
        info.multiplier = channel.getMultiplier();
        info.overflowValue = channel.getOverflow();
        info.flowUnit = channel.getPhenomenon().getUnit().isFlowUnit() ? "flow" : "volume";
        info.obisCode = channel.getObisCode();
        info.nbrOfFractionDigits = channel.getChannelSpec().getNbrOfFractionDigits();
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