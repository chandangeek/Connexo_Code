/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.engine.config.ComPort;

import java.time.Instant;

interface ComPortServerProcess extends ServerProcess {

    /**
     * Gets the used ComPort.
     *
     * @return The ComPort
     */
    ComPort getComPort();

    /**
     * The name of the <i>Thread</i> this process is running in.
     *
     * @return The name of the process' thread
     */
    String getThreadName();

    /**
     * @return the number of threads used by the process.
     */
    public int getThreadCount();

    /**
     * Gets the instant in time of the last registered activity for this process.
     *
     * @return The instant in time
     */
    Instant getLastActivityTimestamp();

}