package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.rest.ObisCodeAdapter;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.masterdata.rest.PhenomenonInfo;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * JSON representation of a channel
 * Created by bvn on 8/6/14.
 */
public class ChannelInfo {
    public long id;
    public String name;
    public TimeDurationInfo interval;
    public PhenomenonInfo unitOfMeasure;
    public Date lastReading;
    public String cimReadingType;
    @XmlJavaTypeAdapter(ObisCodeAdapter.class)
    public ObisCode obisCode;
    public BigDecimal multiplier;
    public BigDecimal overflowValue;
    public String flowUnit;

    public static ChannelInfo from(Channel channel) {
        ChannelInfo info = new ChannelInfo();
        info.id=channel.getId();
        info.name=channel.getName();
        info.interval=new TimeDurationInfo(channel.getInterval());
        info.unitOfMeasure=PhenomenonInfo.from(channel.getPhenomenon());
        info.lastReading=channel.getLastReading();
        info.cimReadingType=channel.getReadingType().getMRID();
        info.multiplier=channel.getMultiplier();
        info.overflowValue=channel.getOverflow();
        info.flowUnit=channel.getPhenomenon().getUnit().isFlowUnit()?"flow":"volume";
        info.obisCode=channel.getObisCode();
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
            info.id=channel.getId();
            info.name=channel.getName();
            info.cimReadingType=channel.getReadingType().getMRID();
            channelInfo.add(info);
        }
        return channelInfo;
    }

}
