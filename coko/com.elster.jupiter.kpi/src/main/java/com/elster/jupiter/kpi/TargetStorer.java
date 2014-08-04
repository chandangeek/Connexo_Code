package com.elster.jupiter.kpi;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Allows storing dynamic target values for a KpiMember.
 */
public interface TargetStorer {

    /**
     * Registers the given target for the given timestamp.
     * @param timestamp
     * @param target
     * @return this, to enable chaining.
     */
    TargetStorer add(Date timestamp, BigDecimal target);

    /**
     * Executes storing all registered dynmaic target values.
     */
    void execute();
}
