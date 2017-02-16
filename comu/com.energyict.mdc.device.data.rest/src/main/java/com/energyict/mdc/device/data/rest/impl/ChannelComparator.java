/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.Channel;

import java.util.Comparator;

/**
 * Created by bvn on 8/7/14.
 */
public class ChannelComparator implements Comparator<Channel> {

    @Override
    public int compare(Channel o1, Channel o2) {
        return o1.getName().compareToIgnoreCase(o2.getName());
    }
}
