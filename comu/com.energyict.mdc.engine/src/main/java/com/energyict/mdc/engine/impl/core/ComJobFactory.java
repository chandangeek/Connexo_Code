/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;

import java.util.Iterator;
import java.util.List;

public interface ComJobFactory {

    /**
     * Consumes the {@link com.energyict.mdc.device.data.tasks.ComTaskExecution}s from the ResultSet
     * and wraps them in the appropriate {@link com.energyict.mdc.engine.impl.core.ComJob}s.
     *
     *
     * @param comTaskExecutions the fetched ComTaskExecutions
     */
    List<ComJob> consume(Iterator<ComTaskExecution> comTaskExecutions);

}