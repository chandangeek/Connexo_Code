package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.Channel;

import java.util.Comparator;

/**
 */
public class ChannelComparator implements Comparator<Channel> {

    @Override
    public int compare(Channel o1, Channel o2) {
        return o1.getMainReadingType().getName().compareToIgnoreCase(o2.getMainReadingType().getName());
    }
}
