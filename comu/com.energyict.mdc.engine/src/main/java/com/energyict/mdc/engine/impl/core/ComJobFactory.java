/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.tasks.ComTaskExecution;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public interface ComJobFactory {

    /**
     * Consumes the {@link ComTaskExecution}s from the ResultSet
     * and wraps them in the appropriate {@link com.energyict.mdc.engine.impl.core.ComJob}s.
     *
     *
     * @param comTaskExecutions the fetched ComTaskExecutions
     */
    List<ComJob> consume(Iterator<ComTaskExecution> comTaskExecutions);

    /**
     * Collects all the {@link ComTaskExecution}s from the ResultSet
     * and wraps them in the appropriate {@link com.energyict.mdc.engine.impl.core.ComJob}s.
     *
     * @param comTaskExecutions the fetched ComTaskExecutions
     */
    List<ComJob> collect(Iterator<ComTaskExecution> comTaskExecutions);

    default boolean consume(ComTaskExecution comTaskExecution) {
        return false;
    }

    default List<ComJob> getJobs() {
        return Collections.EMPTY_LIST;
    }

}