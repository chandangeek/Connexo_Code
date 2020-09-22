/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@ProviderType
public interface ReadingQualityFetcher extends ReadingQualityFilter {

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
