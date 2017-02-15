/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@ProviderType
public interface ReadingQualityFetcher extends ReadingQualityTypeFilter {

    /**
     * Defines the time interval to search reading qualities in.
     * Pls use exclusively with {@link #atTimestamp(Instant)} because these criteria intersect and not unite.
     * None of these 2 methods called is equivalent to setting {@code interval = Range.all()}.
     *
     * @param interval The time interval to search reading qualities in.
     * @return The self to proceed with search criteria definition or collect the results.
     */
    ReadingQualityFetcher inTimeInterval(Range<Instant> interval);

    /**
     * Defines the timestamp to search reading qualities at.
     * Pls use exclusively with {@link #inTimeInterval(Range)} because these criteria intersect and not unite
     * None of these 2 methods called is equivalent to calling {@link #inTimeInterval(Range)} on {@code Range.all()}.
     *
     * @param timestamp The timestamp to search reading qualities at.
     * @return The self to proceed with search criteria definition or collect the results.
     */
    ReadingQualityFetcher atTimestamp(Instant timestamp);

    /**
     * Assures searching actual reading qualities only.
     *
     * @return The self to proceed with search criteria definition or collect the results.
     */
    ReadingQualityFetcher actual();

    /**
     * Assures sorting the reading qualities chronologically by reading timestamp.
     *
     * @return The self to proceed with search criteria definition or collect the results.
     */
    ReadingQualityFetcher sorted();

    /**
     * Terminates search criteria definition and collects the results.
     *
     * @return The list of matched {@link ReadingQualityRecord ReadingQualityRecords}.
     */
    List<ReadingQualityRecord> collect();

    /**
     * Terminates search criteria definition and returns the {@link Stream} of found {@link ReadingQualityRecord ReadingQualityRecords}.
     *
     * @return The {@link Stream} of found {@link ReadingQualityRecord ReadingQualityRecords}.
     */
    default Stream<ReadingQualityRecord> stream() {
        return collect().stream();
    }

    /**
     * Terminates search criteria definition and collects the results.
     *
     * @return The first matched {@link ReadingQualityRecord} if any.
     */
    Optional<ReadingQualityRecord> findFirst();

    /**
     * Terminates search criteria definition and answers if there's at least a quality matching.
     *
     * @return {@code true} if there's at least one {@link ReadingQualityRecord} matching, {@code false} otherwise.
     */
    default boolean anyMatch() {
        return findFirst().isPresent();
    }

    /**
     * Terminates search criteria definition and answers if there're no qualities matching.
     *
     * @return {@code true} if there's no any {@link ReadingQualityRecord} matching, {@code false} otherwise.
     */
    default boolean noneMatch() {
        return !findFirst().isPresent();
    }
}
