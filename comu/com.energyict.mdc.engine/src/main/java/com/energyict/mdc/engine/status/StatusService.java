/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.status;

/**
 * Provides services to obtain status information of the {@link com.energyict.mdc.engine.config.ComServer}
 * that is configured to run in this instance of the MDC application.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-18 (10:45)
 */
public interface StatusService {

    /**
     * Gets the status of the {@link com.energyict.mdc.engine.config.ComServer}
     * that is configured to run in this instance of the MDC application.
     * Note that this never returns <code>null</code>,
     * even when that ComServer is not running at all,
     * you will get a ComServerStatus that will return <code>true</code>
     * in {@link ComServerStatus#isRunning()}.
     *
     * @return The ComServerStatus
     */
    public ComServerStatus getStatus();

}