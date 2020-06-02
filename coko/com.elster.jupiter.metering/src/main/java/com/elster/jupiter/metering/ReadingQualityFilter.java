/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

@ProviderType
public interface ReadingQualityFilter extends ReadingQualityTypeFilter {

    ReadingQualityFetcher inChannels(Set<Channel> channels);

    ReadingQualityFetcher inScope(Map<Channel, Range<Instant>> scope);

    ReadingQualityFetcher forReadingType(ReadingType readingType);

    ReadingQualityFetcher forReadingTypes(Set<ReadingType> readingTypes);

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
}
