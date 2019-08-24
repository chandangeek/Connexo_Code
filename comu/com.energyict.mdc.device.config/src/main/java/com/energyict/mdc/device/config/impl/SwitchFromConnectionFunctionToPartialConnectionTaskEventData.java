/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.protocol.ConnectionFunction;
import com.energyict.mdc.common.tasks.PartialConnectionTask;

/**
 * @author Stijn Vanhoorelbeke
 * @since 04.07.17 - 17:04
 */
public class SwitchFromConnectionFunctionToPartialConnectionTaskEventData extends ConnectionStrategyChangeEventData {

    private final long oldConnectionFunctionId;
    private final long newPartialConnectionTaskId;

    public SwitchFromConnectionFunctionToPartialConnectionTaskEventData(ComTaskEnablement comTaskEnablement, ConnectionFunction oldConnectionFunction, PartialConnectionTask newPartialConnectionTask) {
        super(comTaskEnablement.getId());
        this.oldConnectionFunctionId = oldConnectionFunction.getId();
        this.newPartialConnectionTaskId = newPartialConnectionTask.getId();
    }

    @Override
    protected ConnectionStrategyEventType getEventType() {
        return ConnectionStrategyEventType.COMTASKENABLEMENT_SWITCH_FROM_CONNECTION_FUNCTION_TO_TASK;
    }

    public long getOldConnectionFunctionId() {
        return oldConnectionFunctionId;
    }

    public long getNewPartialConnectionTaskId() {
        return newPartialConnectionTaskId;
    }
}