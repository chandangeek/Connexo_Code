/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

/**
 * Models the behavior of a process that is running on the server.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-21 (09:09)
 */
public interface ServerProcess {

    /**
     * Gets the {@link ServerProcessStatus} of this ServerProcess.
     *
     * @return The ServerProcessStatus
     */
    ServerProcessStatus getStatus();

    /**
     * Starts this ServerProcess.
     */
    void start();

    /**
     * Shuts down this ServerProcess, possibly waiting
     * for running and/or waiting tasks to complete.
     */
    void shutdown();

    /**
     * Shuts down this ServerProcess as soon as possible,
     * possibly interrupting running tasks and/or
     * skipping waiting tasks.
     */
    void shutdownImmediate();

}