/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.Channel;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class ChannelInfos {
    public int total;
    public List<ChannelInfo> channelInfos = new ArrayList<>();

    ChannelInfos() {
    }

    ChannelInfos(Channel channel) {
        add(channel);
    }

    ChannelInfos(Iterable<? extends Channel> channelInfos) {
        addAll(channelInfos);
    }

    ChannelInfo add(Channel channel) {
        ChannelInfo result = new ChannelInfo(channel);
        channelInfos.add(result);
        total++;
        return result;
    }

    void addAll(Iterable<? extends Channel> channels) {
        for (Channel each : channels) {
            add(each);
        }
    }

}
