/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;

import java.util.List;

public class ChangeQueueSignal extends ScheduledJobImpl{

    public ChangeQueueSignal() {
        super(null, null, null, null);
    }

    @Override
    public List<ComTaskExecution> getComTaskExecutions() {
        return null;
    }

    @Override
    public boolean attemptLock() {
        return false;
    }

    @Override
    public void unlock() {

    }

    @Override
    public boolean isStillPending() {
        return false;
    }

    @Override
    public void execute() {

    }

    @Override
    public ScheduledConnectionTask getConnectionTask() {
        return null;
    }
}
