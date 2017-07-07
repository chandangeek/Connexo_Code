/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.protocol.api.ConnectionFunction;

/**
 * @author Stijn Vanhoorelbeke
 * @since 04.07.17 - 17:07
 */
public class SwitchOnUsingConnectionFunctionEventData extends ConnectionStrategyChangeEventData {

    private final long newConnectionFunctionId;

    public SwitchOnUsingConnectionFunctionEventData(ComTaskEnablement comTaskEnablement, ConnectionFunction newConnectionFunction) {
        super(comTaskEnablement.getId());
        newConnectionFunctionId = newConnectionFunction.getId();
    }

    @Override
    protected ConnectionStrategyEventType getEventType() {
        return ConnectionStrategyEventType.COMTASKENABLEMENT_SWITCH_ON_CONNECTION_FUNCTION;
    }

    public long getNewConnectionFunctionId() {
        return newConnectionFunctionId;
    }
}
