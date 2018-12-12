/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.logging;

/**
 * The supported logging levels for the Configuration annotation.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-09 (08:48)
 */
public enum LogLevel {
    /**
     * Shows only error messages that are the result of serious problems
     * that have occured during the process. Typically, there is no
     * recovery from these types of problems and the current
     * process will likely have stopped or been abandonned.
     */
    ERROR,

    /**
     * Shows only warning messages that are indicators for potential problems
     * that may occur later on in the process.
     */
    WARN,

    /**
     * Shows only informational messages that provide high level
     * understanding of what the process is doing.
     */
    INFO,

    /**
     * Fairly detailed log level, typically used for diagnosing/debugging problems.
     */
    DEBUG,

    /**
     * The most detailed log level, showing all possible messages.
     */
    TRACE
}