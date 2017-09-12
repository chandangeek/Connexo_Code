/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.protocol.api.ConnectionFunction;

/**
 * @author Stijn Vanhoorelbeke
 * @since 04.07.17 - 17:07
 */
public class SwitchFromPartialConnectionTaskToConnectionFunctionEventData extends ConnectionStrategyChangeEventData {

    private final long oldPartialConnectionTaskId;
    private final long newConnectionFunctionId;

    public SwitchFromPartialConnectionTaskToConnectionFunctionEventData(ComTaskEnablement comTaskEnablement, PartialConnectionTask oldPartialConnectionTask, ConnectionFunction newConnectionFunction) {
        super(comTaskEnablement.getId());
        this.oldPartialConnectionTaskId = oldPartialConnectionTask.getId();
        this.newConnectionFunctionId = newConnectionFunction.getId();
    }

    @Override
    protected ConnectionStrategyEventType getEventType() {
        return ConnectionStrategyEventType.COMTASKENABLEMENT_SWITCH_FROM_TASK_TO_CONNECTION_FUNCTION;
    }

    public long getOldPartialConnectionTaskId() {
        return oldPartialConnectionTaskId;
    }

    public long getNewConnectionFunctionId() {
        return newConnectionFunctionId;
    }
}
