/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

/**
 * Does periodic cleanup of outdated {@link com.energyict.mdc.device.data.tasks.ComTaskExecutionTrigger}s<br/>
 * All triggers who have a trigger timestamp older than one day in the past will be removed from the database,
 * as these triggers are no longer relevant.
 *
 * @author sva
 * @since 2016-06-29 (17:05)
 */
interface ComServerCleanupProcess extends ServerProcess {
}