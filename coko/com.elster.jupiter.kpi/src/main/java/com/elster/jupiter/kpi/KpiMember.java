package com.elster.jupiter.kpi;

import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * One specific metric within a Kpi, which has a separate name and its own target (if any).
 */
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
     * @param date
     * @return the target value for the given time. Members with a static target will disregard the date.
     */
    BigDecimal getTarget(Date date);

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
     * @param date
     * @param bigDecimal
     */
    void score(Date date, BigDecimal bigDecimal);

    /**
     * @param date
     * @return an Optional containing the score (if any) on the given time.
     */
    Optional<KpiEntry> getScore(Date date);

    /**
     * @param interval
     * @return a List containing all available scores in the given Interval
     */
    List<? extends KpiEntry> getScores(Interval interval);

    /**
     * @return a TargetStorer onstance to register target values for this pki metric. Will throw an IllegalStateException if this metric has a static target.
     */
    TargetStorer getTargetStorer();

    /**
     * Updates the static target for this Kpi metric. Will throw an IllegalStateException if this metric has a dynamic target.
     * @param target
     */
    void updateTarget(BigDecimal target);

}
