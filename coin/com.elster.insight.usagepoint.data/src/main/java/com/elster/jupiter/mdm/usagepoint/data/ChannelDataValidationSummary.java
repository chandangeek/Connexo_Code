package com.elster.jupiter.mdm.usagepoint.data;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Map;

/**
 * An interface to hold channel data validation statistics:
 * counts of {@link IChannelDataValidationSummaryFlag ChannelDataValidationSummaryFlags} in some interval
 * as well as total number.
 */
@ProviderType
public interface ChannelDataValidationSummary {
    /**
     * Returns a {@link Map} of {@link IChannelDataValidationSummaryFlag} counts;
     * if any flag is not found, it is not present in the map.
     * @return A {@link Map} of {@link IChannelDataValidationSummaryFlag} counts;
     * if any flag is not found, it is not present in the map.
     */
    Map<IChannelDataValidationSummaryFlag, Integer> getValues();

    /**
     * Returns the entire count of all {@link IChannelDataValidationSummaryFlag ChannelDataValidationSummaryFlags}.
     * @return The entire count of all {@link IChannelDataValidationSummaryFlag ChannelDataValidationSummaryFlags}.
     */
    int getSum();

    /**
     * Returns the target interval used to gather statistics.
     * @return The target interval used to gather statistics.
     */
    Range<Instant> getTargetInterval();

    /**
     * Returns the type of statistics.
     * @return The type of statistics.
     */
    ChannelDataValidationSummaryType getType();
}
