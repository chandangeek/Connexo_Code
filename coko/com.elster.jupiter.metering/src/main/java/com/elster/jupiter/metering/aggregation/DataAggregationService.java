/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.aggregation;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.readings.BaseReading;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides data aggregation services that are currently
 * designed for {@link UsagePoint}s. Support for
 * service delivery point and zone will be introduced.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-04 (09:31)
 */
@ProviderType
public interface DataAggregationService {

    /**
     * Calculates the measurement data of the specified {@link MetrologyContract}
     * using the measurement data that is provided by the meters that have been
     * activated on the specified {@link UsagePoint}.<br>
     * Note that the requested period is clipped to the period during which
     * the MetrologyContract was active on the UsagePoint and will throw
     * a {@link MetrologyContractDoesNotApplyToUsagePointException}
     * when the MetrologyContract does not apply to the UsagePoint.
     *
     * @param usagePoint The UsagePoint
     * @param contract The MetrologyContract
     * @param period The period in time that should be taken into consideration
     * @return The CalculatedMetrologyContractData
     */
    CalculatedMetrologyContractData calculate(UsagePoint usagePoint, MetrologyContract contract, Range<Instant> period);

    /**
     * Introspects the calculation of the measurement data of the specified {@link MetrologyContract}
     * by returning the {@link com.elster.jupiter.metering.Channel}s that contain the measurement data
     * that is provided by the meters that have been activated on the specified {@link UsagePoint}.<br>
     * Note that the requested period is clipped to the period during which
     * the MetrologyContract was active on the UsagePoint and will throw
     * a {@link MetrologyContractDoesNotApplyToUsagePointException}
     * when the MetrologyContract does not apply to the UsagePoint.
     *
     * @param usagePoint The UsagePoint
     * @param contract The MetrologyContract
     * @param period The period in time that should be taken into consideration
     * @return The MetrologyContractIntrospector
     */
    MetrologyContractCalculationIntrospector introspect(UsagePoint usagePoint, MetrologyContract contract, Range<Instant> period);

    /**
     * Supports transactional editing of multiple readings on a single UsagePoint and {@link MetrologyContract}.
     *
     * @param usagePoint The UsagePoint
     * @param contract The MetrologyContract
     * @param system The {@link QualityCodeSystem} that handles removal, estimation, confirmation and editing.
     * @return The MetrologyContractDateEditor
     */
    MetrologyContractDataEditor edit(UsagePoint usagePoint, MetrologyContract contract, ReadingTypeDeliverable deliverable, QualityCodeSystem system);

    /**
     * Supports transactional editing of multiple readings on a single UsagePoint and {@link MetrologyContract}.
     * Editing data that is outside the period during which the MetrologyContract is applicable
     * to the UsagePoint will throw a {@link MetrologyContractDoesNotApplyToUsagePointException}.
     */
    @ProviderType
    interface MetrologyContractDataEditor {
        /**
         * Returns the UsagePoint whose readings are being edited.
         *
         * @return The UsagePoint
         */
        UsagePoint getUsagePoint();

        /**
         * Removes all existing {@link BaseReading}s on the specified timestamps.
         * Note that this is a convenience method for {@link #removeTimestamps(Set)}
         * that will find the BaseReading on the appropriate Channel first.
         *
         * @param readingTimestamps The timestamps of all the BaseReadings that you want to remove
         * @return This MetrologyContractDateEditor to support method chaining
         * @see #removeTimestamps(Set)
         * @see com.elster.jupiter.metering.Channel#removeReadings(QualityCodeSystem, List)
         */
        default MetrologyContractDataEditor remove(Instant... readingTimestamps) {
            return removeTimestamps(Stream.of(readingTimestamps).collect(Collectors.toSet()));
        }

        /**
         * Removes all existing {@link BaseReading}s on the specified timestamps.
         * Note that this is a convenience method for {@link #remove(BaseReadingRecord...)}
         * that will find the BaseReading on the appropriate Channel first.
         *
         * @param readingTimestamps The timestamp of the BaseReading that you want to remove
         * @return This MetrologyContractDateEditor to support method chaining
         * @see com.elster.jupiter.metering.Channel#removeReadings(QualityCodeSystem, List)
         */
        MetrologyContractDataEditor removeTimestamps(Set<Instant> readingTimestamps);

        /**
         * Removes all existing {@link BaseReading}s on the specified timestamps.
         * Note that this is a convenience method for {@link #removeReadings(Set)}.
         *
         * @param readings The List of the BaseReading that you want to remove
         * @return This MetrologyContractDateEditor to support method chaining
         * @see #removeReadings(Set)
         * @see com.elster.jupiter.metering.Channel#removeReadings(QualityCodeSystem, List)
         */
        default MetrologyContractDataEditor remove(BaseReadingRecord... readings) {
            return removeReadings(Stream.of(readings).collect(Collectors.toSet()));
        }

        /**
         * Removes the specified {@link BaseReading}s.
         *
         * @param readings The List of the BaseReading that you want to remove
         * @return This MetrologyContractDateEditor to support method chaining
         * @see com.elster.jupiter.metering.Channel#removeReadings(QualityCodeSystem, List)
         */
        MetrologyContractDataEditor removeReadings(Set<BaseReadingRecord> readings);

        /**
         * Estimates the specified {@link BaseReading}s.
         * Note that this is a convenience method for {@link #estimateAll(List)}.
         *
         * @param readings The readings
         * @return This MetrologyContractDateEditor to support method chaining
         * @see #estimateAll(List)
         * @see com.elster.jupiter.metering.Channel#estimateReadings(QualityCodeSystem, List)
         */
        default MetrologyContractDataEditor estimate(BaseReading... readings) {
            return estimateAll(Arrays.asList(readings));
        }

        /**
         * Estimates the specified {@link BaseReading}s.
         *
         * @param readings The readings
         * @return This MetrologyContractDateEditor to support method chaining
         * @see com.elster.jupiter.metering.Channel#estimateReadings(QualityCodeSystem, List)
         */
        MetrologyContractDataEditor estimateAll(List<BaseReading> readings);

        /**
         * Confirms the specified {@link BaseReading}s.
         * Note that this is a convenience method for {@link #confirmAll(List)}.
         *
         * @param readings The readings
         * @return This MetrologyContractDateEditor to support method chaining
         * @see #confirmAll(List)
         * @see com.elster.jupiter.metering.Channel#confirmReadings(QualityCodeSystem, List)
         */
        default MetrologyContractDataEditor confirm(BaseReading... readings) {
            return confirmAll(Arrays.asList(readings));
        }

        /**
         * Confirms the specified {@link BaseReading}s.
         *
         * @param readings The readings
         * @return This MetrologyContractDateEditor to support method chaining
         * @see com.elster.jupiter.metering.Channel#confirmReadings(QualityCodeSystem, List)
         */
        MetrologyContractDataEditor confirmAll(List<BaseReading> readings);

        /**
         * Updates the specified {@link BaseReading}s.
         * Note that this is a convenience method for {@link #updateAll(List)}.
         *
         * @param readings The readings
         * @return This MetrologyContractDateEditor to support method chaining
         * @see #updateAll(List)
         * @see com.elster.jupiter.metering.Channel#editReadings(QualityCodeSystem, List)
         */
        default MetrologyContractDataEditor update(BaseReading... readings) {
            return updateAll(Arrays.asList(readings));
        }

        /**
         * Updates the specified {@link BaseReading}s.
         *
         * @param readings The readings
         * @return This MetrologyContractDateEditor to support method chaining
         * @see com.elster.jupiter.metering.Channel#editReadings(QualityCodeSystem, List)
         */
        MetrologyContractDataEditor updateAll(List<BaseReading> readings);

        /**
         * Saves all changes that have been applied in a single transaction.
         * Once saved, this MetrologyContractDateEditor can no longer be used
         * and will in fact throw IllegalStateException when an attempt
         * is made to make additional changes or save again.
         * In other words, this is the terminal operation.
         */
        void save();
    }

}