/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard;

import aQute.bnd.annotation.ProviderType;

/**
 * Bundles a number of {@link TaskStatusBreakdownCounter}s that will track occurrences of things.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (09:28)
 */
@ProviderType
public interface TaskStatusBreakdownCounters<C> extends Iterable<TaskStatusBreakdownCounter<C>> {

    long getTotalSuccessCount();

    long getTotalFailedCount();

    long getTotalPendingCount();

    long getTotalCount();

}