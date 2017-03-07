/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.Channel;

import java.util.function.Predicate;

/**
 * Search criteria is an abstraction to implement different filtering for collections of {@link Channel} objects.<br>
 * Supposed to be used to search channels or registers.
 */
public interface UsagePointChannelSearchCriteria {

    /**
     * Provides {@link Predicate} supposed to be used to filter {@link java.util.stream.Stream} streams of
     * {@link Channel} objects.<br>
     * Filtered stream will contain sutable channels related to particular factory instance.
     *
     * @return {@link Predicate} instance
     */
    Predicate<Channel> getFilterPredicate();

    /**
     * Provides {@link MessageSeeds} value for appropriate message in case when element has not been found
     *
     * @return {@link MessageSeeds} value
     */
    MessageSeeds getNoSuchElementMessageSeed();
}
