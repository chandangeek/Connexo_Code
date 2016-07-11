package com.energyict.mdc.device.data.rest.impl;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.HashSet;

@XmlRootElement
public class ChannelHistoryInfos {
    @XmlElement
    public Collection<ChannelHistoryInfo> channelHistory = new HashSet<>();
}
