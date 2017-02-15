/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation for the {@link ComJob} interface
 * that represents a group of {@link ComTaskExecution}s that need to be
 * executed one after the other.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-07-20 (14:06)
 */
public class ComTaskExecutionGroup implements ComJob {

    private long connectionTaskId;
    private List<ComTaskExecution> comTaskExecutions = new ArrayList<>();

    // For xml serialization purposes only
    public ComTaskExecutionGroup() {
        super();
    }

    public ComTaskExecutionGroup(long connectionTaskId) {
        this();
        this.connectionTaskId = connectionTaskId;
    }

    @Override
    public long getConnectionTaskId() {
        return connectionTaskId;
    }

    public void add(ComTaskExecution comTask) {
        this.comTaskExecutions.add(comTask);
    }

    @Override
    public List<ComTaskExecution> getComTaskExecutions() {
        List<ComTaskExecution> comTaskExecutions = new ArrayList<>();
        comTaskExecutions.addAll(this.comTaskExecutions);
        return comTaskExecutions;
    }
}