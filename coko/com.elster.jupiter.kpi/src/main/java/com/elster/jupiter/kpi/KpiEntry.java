package com.elster.jupiter.kpi;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Models a score and matching target at a certain time.
 */
public interface KpiEntry {

    /**
     * @return Date representing the time of the score and target
     */
    Instant getTimestamp();

    /**
     * @return the kpi score
     */
    BigDecimal getScore();

    /**
     * @return the kpi target
     */
    BigDecimal getTarget();

    /**
     * @return true if the score meets the target, false otherwise.
     */
    boolean meetsTarget();
}
