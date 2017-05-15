/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.Channel;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

import java.time.Instant;

public interface ChannelValidation {

    long getId();

    ChannelsContainerValidation getChannelsContainerValidation();

    Instant getLastChecked();

    Channel getChannel();

    boolean hasActiveRules();

    boolean updateLastChecked(Instant date);

    boolean moveLastCheckedBefore(Instant date);

    void validate(RangeSet<Instant> ranges);

    boolean isLastValidationComplete();
}
