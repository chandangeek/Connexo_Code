package com.elster.jupiter.mdm.usagepoint.data;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Map;

/**
 * An interface to hold channel data validation statistics:
 * counts of {@link IChannelDataCompletionSummaryFlag ChannelDataValidationSummaryFlags} in some interval
 * as well as total number.
 */
@ProviderType
public interface IChannelDataCompletionSummary {
    /**
     * Returns a {@link Map} of {@link IChannelDataCompletionSummaryFlag} counts;
     * if any flag is not found, it is not present in the map.
     * @return A {@link Map} of {@link IChannelDataCompletionSummaryFlag} counts;
     * if any flag is not found, it is not present in the map.
     */
    Map<IChannelDataCompletionSummaryFlag, Integer> getValues();

    /**
     * Returns the entire count of all {@link IChannelDataCompletionSummaryFlag ChannelDataValidationSummaryFlags}.
     * @return The entire count of all {@link IChannelDataCompletionSummaryFlag ChannelDataValidationSummaryFlags}.
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
    ChannelDataCompletionSummaryType getType();
}
