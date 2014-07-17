package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.config.ChannelSpec;

import java.util.Comparator;

public class LoadProfileChannelComparator implements Comparator<ChannelSpec> {

    @Override
    public int compare(ChannelSpec o1, ChannelSpec o2) {
        return o1.getChannelType().getName().compareToIgnoreCase(o2.getChannelType().getName());
    }
}