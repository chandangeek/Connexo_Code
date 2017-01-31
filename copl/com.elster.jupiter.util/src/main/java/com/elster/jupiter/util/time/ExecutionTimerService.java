/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.time;

import aQute.bnd.annotation.ProviderType;

import java.time.Duration;

/**
 * Creates {@link ExecutionTimer}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-03-10 (10:13)
 */
@ProviderType
public interface ExecutionTimerService {

    /**
     * Creates a new ExecutionTimer with the specified unique name
     * that will consider blocks of code that execute longer than
     * the specified Duration as timed-out.
     *
     * @param name The name
     * @param timeout The Duration
     * @return The ExecutionTimer
     * @throws IllegalArgumentException Thrown when another ExecutionTimer with the same name already exists
     */
    ExecutionTimer newTimer(String name, Duration timeout);

}