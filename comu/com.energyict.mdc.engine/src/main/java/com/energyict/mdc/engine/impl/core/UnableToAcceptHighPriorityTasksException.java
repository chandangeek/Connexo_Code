/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

/**
 * Models the exceptional situation that occurs when the scheduler
 * first checked that a {@link RunningComServer} was able to accept
 * high priority tasks then queried for work but then when it
 * attempts to execute one of the high priority tasks
 * the RunningComServer can no longer accept it.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-10-16 (14:59)
 */
public class UnableToAcceptHighPriorityTasksException extends RuntimeException {
}