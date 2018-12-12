/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.protocol.api.ConnectionFunction;

/**
 * @author Stijn Vanhoorelbeke
 * @since 04.07.17 - 17:03
 */
public class SwitchBetweenConnectionFunctionsEventData extends ConnectionStrategyChangeEventData {

    private final long oldConnectionFunctionId;
    private final long newConnectionFunctionId;

    public SwitchBetweenConnectionFunctionsEventData(ComTaskEnablement comTaskEnablement, ConnectionFunction oldConnectionFunction, ConnectionFunction newConnectionFunction) {
        super(comTaskEnablement.getId());
        this.oldConnectionFunctionId = oldConnectionFunction.getId();
        this.newConnectionFunctionId = newConnectionFunction.getId();
    }

    @Override
    protected ConnectionStrategyEventType getEventType() {
        return ConnectionStrategyEventType.COMTASKENABLEMENT_SWITCH_BETWEEN_CONNECTION_FUNCTIONS;
    }

    public long getOldConnectionFunctionId() {
        return oldConnectionFunctionId;
    }

    public long getNewConnectionFunctionId() {
        return newConnectionFunctionId;
    }
}
