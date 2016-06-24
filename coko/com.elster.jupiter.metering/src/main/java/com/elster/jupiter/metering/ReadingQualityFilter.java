package com.elster.jupiter.metering;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ProviderType
public interface ReadingQualityFilter extends ReadingQualityTypeFilter {

    /**
     * Defines the time interval to search reading qualities in.
     * Pls use exclusively with {@link #atTimestamp(Instant)} because these criteria intersect and not unite.
     * None of these 2 methods called is equivalent to setting <code>interval = Range.all()</code>
     *
     * @param interval the time interval to search reading qualities in.
     * @return the self to proceed with search criteria definition or collect the results.
     */
    ReadingQualityFilter inTimeInterval(Range<Instant> interval);

    /**
     * Defines the timestamp to search reading qualities at.
     * Pls use exclusively with {@link #inTimeInterval(Range)} because these criteria intersect and not unite
     * None of these 2 methods called is equivalent to calling {@link #inTimeInterval(Range)} on <code>Range.all()</code>
     *
     * @param timestamp the timestamp to search reading qualities at.
     * @return the self to proceed with search criteria definition or collect the results.
     */
    ReadingQualityFilter atTimestamp(Instant timestamp);

    /**
     * Assures searching actual reading qualities only.
     *
     * @return the self to proceed with search criteria definition or collect the results.
     */
    ReadingQualityFilter actual();

    /**
     * Assures sorting the reading qualities chronologically by reading timestamp.
     *
     * @return the self to proceed with search criteria definition or collect the results.
     */
    ReadingQualityFilter sorted();

    /**
     * Terminates search criteria definition and collects the results.
     *
     * @return the list of matched {@link ReadingQualityRecord ReadingQualityRecords}.
     */
    List<ReadingQualityRecord> collect();

    /**
     * Terminates search criteria definition and collects the results.
     *
     * @return the first matched {@link ReadingQualityRecord} if any.
     */
    Optional<ReadingQualityRecord> findFirst();
}
