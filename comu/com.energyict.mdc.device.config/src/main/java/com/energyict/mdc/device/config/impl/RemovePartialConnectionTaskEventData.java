/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.PartialConnectionTask;

/**
 * Models the data that is involved in event that are produced when
 * a {@link com.energyict.mdc.device.config.ComTaskEnablement} switches
 * from using a {@link com.energyict.mdc.device.config.PartialConnectionTask}
 * to using the default connection task.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-18 (15:10)
 */
public class RemovePartialConnectionTaskEventData extends ConnectionStrategyChangeEventData {
    private final long partialConnectionTaskId;

    public RemovePartialConnectionTaskEventData(ComTaskEnablement comTaskEnablement, PartialConnectionTask partialConnectionTask) {
        super(comTaskEnablement.getId());
        this.partialConnectionTaskId = partialConnectionTask.getId();
    }

    @Override
    protected ConnectionStrategyEventType getEventType() {
        return ConnectionStrategyEventType.COMTASKENABLEMENT_REMOVE_TASK;
    }

    public long getPartialConnectionTaskId() {
        return partialConnectionTaskId;
    }

}