/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.Channel;

import java.util.function.Predicate;

/**
 * Represents different types of {@link Channel}
 */
public interface UsagePointChannelRepresentationType {

    Predicate<Channel> getFilterPredicate();

    MessageSeeds getNoSuchElementMessageSeed();

}

