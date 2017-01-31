/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Map;

/**
 * An interface to hold channel data validation statistics:
 * counts of {@link ChannelDataValidationSummaryFlag ChannelDataValidationSummaryFlags} in some interval
 * as well as total number.
 */
@ProviderType
public interface ChannelDataValidationSummary {
    /**
     * Returns a {@link Map} of {@link ChannelDataValidationSummaryFlag} counts;
     * if any flag is not found, it is not present in the map.
     * @return A {@link Map} of {@link ChannelDataValidationSummaryFlag} counts;
     * if any flag is not found, it is not present in the map.
     */
    Map<ChannelDataValidationSummaryFlag, Integer> getValues();

    /**
     * Returns the entire count of all {@link ChannelDataValidationSummaryFlag ChannelDataValidationSummaryFlags}.
     * @return The entire count of all {@link ChannelDataValidationSummaryFlag ChannelDataValidationSummaryFlags}.
     */
    int getSum();

    /**
     * Returns the target interval used to gather statistics.
     * @return The target interval used to gather statistics.
     */
    Range<Instant> getTargetInterval();
}
