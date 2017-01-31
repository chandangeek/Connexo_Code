/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.time;

import aQute.bnd.annotation.ProviderType;

/**
 * Models the statistical information gathered by an {@link ExecutionTimer}.
 * Measurements are done with nano second precission.
 */
@ProviderType
public interface ExecutionStatistics {

    /**
     * The number of blocks of code that have been timed by the
     * ExecutionTimer that completed within the expected time,
     * i.e. blocks of code that did not time-out.
     *
     * @return The number of blocks of code that completed timely
     */
    long getCompleteCount();

    /**
     * The number of blocks of code that have been timed by the
     * ExecutionTimer that did not completed within the expected time,
     * i.e. blocks of code that did timed-out.
     *
     * @return The number of blocks of code that timed-out
     */
    long getTimeoutCount();

    /**
     * The total execution time of all blocks of code that have been
     * timed by the ExecutionTimer that completed within the expected time.
     *
     * @return The total execution time of all blocks of code
     */
    long getTotalExecutionTime();

    /**
     * The execution time of fastest block of code that has been
     * timed by the ExecutionTimer that completed within the expected time.
     *
     * @return The execution time of the fastest block of code
     */
    long getMinimumExecutionTime();

    /**
     * The execution time of slowest block of code that has been
     * timed by the ExecutionTimer that completed within the expected time.
     *
     * @return The execution time of the slowest block of code
     */
    long getMaximumExecutionTime();

    /**
     * The average execution time of all blocks of code that have been
     * timed by the ExecutionTimer that completed within the expected time.
     *
     * @return The average execution time of all blocks of code or zero
     *         if no blocks have completed  within the expected time
     */
    long getAverageExecutionTime();

}