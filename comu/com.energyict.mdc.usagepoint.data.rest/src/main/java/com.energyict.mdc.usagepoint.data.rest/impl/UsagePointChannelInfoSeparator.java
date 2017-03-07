/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;

/**
 * Channel info separator is an abstraction to implement different representation for {@link Channel} objects.<br>
 * Currently, channel may be represented as channel or register.
 *
 * @param <T> corresponding info object representing {@link Channel} instance
 */
public interface UsagePointChannelInfoSeparator<T> extends UsagePointChannelSearchCriteria {
    /**
     * Method to build info object to represent {@link Channel}
     *
     * @param channel {@link Channel} instance to be represented as info object
     * @param usagePoint {@link UsagePoint} owns channel
     * @param metrologyConfiguration {@link UsagePointMetrologyConfiguration} related to channel
     * @return corresponding info object
     */
    T from(Channel channel, UsagePoint usagePoint, UsagePointMetrologyConfiguration metrologyConfiguration);
}
