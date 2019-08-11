/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.tasks.PartialConnectionTask;

/**
 * Models the data that is involved in event that are produced when
 * a {@link ComTaskEnablement} switches
 * between 2 {@link PartialConnectionTask}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-18 (15:10)
 */
public class SwitchBetweenPartialConnectionTasksEventData extends ConnectionStrategyChangeEventData {
    private final long oldPartialConnectionTaskId;
    private final long newPartialConnectionTaskId;

    public SwitchBetweenPartialConnectionTasksEventData(ComTaskEnablement comTaskEnablement, PartialConnectionTask oldPartialConnectionTask, PartialConnectionTask newPartialConnectionTask) {
        super(comTaskEnablement.getId());
        this.oldPartialConnectionTaskId = oldPartialConnectionTask.getId();
        this.newPartialConnectionTaskId = newPartialConnectionTask.getId();
    }

    @Override
    protected ConnectionStrategyEventType getEventType() {
        return ConnectionStrategyEventType.COMTASKENABLEMENT_SWITCH_BETWEEN_TASKS;
    }

    public long getOldPartialConnectionTaskId() {
        return oldPartialConnectionTaskId;
    }

    public long getNewPartialConnectionTaskId() {
        return newPartialConnectionTaskId;
    }
}