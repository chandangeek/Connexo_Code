/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kpi;

import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * One specific metric within a Kpi, which has a separate name and its own target (if any).
 */
@ProviderType
public interface KpiMember extends HasName {

    /**
     * @return the Kpi to which this member belongs
     */
    Kpi getKpi();

    /**
     * @return true if this member has a dynamic target, i.e. a target that can vary over time, false otherwise.
     */
    boolean hasDynamicTarget();

    /**
     * @param instant
     * @return the target value for the given time. Members with a static target will disregard the date.
     */
    BigDecimal getTarget(Instant instant);

    /**
     * @return true if the target is a minimum value, false otherwise
     */
    boolean targetIsMinimum();

    /**
     * @return true if the target is a maximum value, false otherwise
     */
    boolean targetIsMaximum();

    /**
     * Registers a score for this Kpi metric at the given date.
     *
     * @param date
     * @param bigDecimal
     */
    void score(Instant date, BigDecimal bigDecimal);

    /**
     * Registers scores for this Kpi metric at given set of dates
     *
     * @param scores the set of dates with there scores
     * @since 2.2
     */
    void score(Map<Instant, BigDecimal> scores);

    /**
     * @param date
     * @return an Optional containing the score (if any) on the given time.
     */
    Optional<KpiEntry> getScore(Instant date);

    /**
     * @param range
     * @return a List containing all available scores in the given Interval
     */
    List<? extends KpiEntry> getScores(Range<Instant> range);

    /**
     * @return a TargetStorer onstance to register target values for this pki metric. Will throw an IllegalStateException if this metric has a static target.
     */
    TargetStorer getTargetStorer();

    /**
     * Updates the static target for this Kpi metric. Will throw an IllegalStateException if this metric has a dynamic target.
     *
     * @param target
     */
    void updateTarget(BigDecimal target);

}
