/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

/**
 * Receives notification that the execution of a {@link ScheduledJob} has started.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-08-30 (10:54)
 */
public interface ScheduledJobExecutionEventListener {

    /**
     * Receives notification that the execution of the {@link ScheduledJob} has started.
     *
     * @param job The ScheduledJob
     */
    public void executionStarted (ScheduledJob job);

}