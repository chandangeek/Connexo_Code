package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.impl.aggregation.IntervalLength;

import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Optional;

/**
 * Adds behavior to {@link Formula} that is specific
 * to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-05 (13:00)
 */
public interface ServerFormula extends Formula {

    /**
     * Delete this {@link Formula}
     */
    void delete();

    /**
     * Get the greatest interval used (as requirement) in the formula
     */
    IntervalLength getIntervalLength();

    /**
     * Get all intervals used (as requirement) in the formula
     */
    List<IntervalLength> getIntervalLengths();
}