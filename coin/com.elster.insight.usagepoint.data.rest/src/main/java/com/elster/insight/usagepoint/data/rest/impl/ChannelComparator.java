package com.elster.insight.usagepoint.data.rest.impl;

import java.util.Comparator;

import com.elster.jupiter.metering.Channel;

/**
 */
public class ChannelComparator implements Comparator<Channel> {

    @Override
    public int compare(Channel o1, Channel o2) {
        return o1.getMainReadingType().getName().compareToIgnoreCase(o2.getMainReadingType().getName());
    }
}
