/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.time;

import aQute.bnd.annotation.ProviderType;

import java.time.Duration;
import java.util.concurrent.Callable;

/**
 * Times the execution of blocks of code and gathers statistics across
 * multiple executions. A block of code is either a Rannable or a Callable.
 * An ExecutionTimer can be configured at creation time with a maximum
 * execution time. Blocks of code that run longer than that are considered
 * to have timed-out and are not taken into account for the overall statistics.
 * The are counted in a separate KPI.
 * <p>
 * When you no longer need an ExecutionTimer, you are expected to deactivate it.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-10 (09:39)
 */
@ProviderType
public interface ExecutionTimer {

    /**
     * Returns the unique name of this ExecutionTimer.
     *
     * @return The name
     */
    String getName();

    /**
     * Gets the Duration after which blocks of code are considered timed-out
     * and are no longer taken into account for the overall statistics.
     *
     * @return The Duration
     */
    Duration getTimeout();

    ExecutionStatistics getStatistics();

    /**
     * Joins the other ExecutionTimer, i.e. all the timings of the other
     * ExecutionTimer are also taken into account by this one.
     * This allows you to create hierarchies of ExecutionTimer
     * that gather execution statistics at different levels.<br>
     * As an example: say you want to time the searches in your aplication
     * but you have different types of searches. Every type of search could
     * have its own timer and you could create a "global" timer that simply
     * joins the timers for each type of search. The glpbal timer would not
     * have to time any blocks of code, it suffices that the timers that
     * he joins are timing the blocks of code.
     *
     * @param other The other ExecutionTimer
     */
    void join(ExecutionTimer other);

    /**
     * Resets the statistics.
     */
    void reset();

    /**
     * Deactivates this ExecutionTimer.
     * All future calls to time blocks of code will
     * thrown an IllegalStateException.
     */
    void deactivate();

    /**
     * Executes the Callable and times its execution.
     *
     * @param callable Thde Callable
     * @param <V> The type of the return value from the Callable
     * @return The result of the Callable
     * @throws Exception Thrown by the Callable
     */
    <V> V time(Callable<V> callable) throws Exception;

    /**
     * Executes the Runnable and times its execution.
     *
     * @param runnable Thde Runnable
     */
   void time(Runnable runnable);

}