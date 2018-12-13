/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.protocol.api.ConnectionFunction;

/**
 * @author Stijn Vanhoorelbeke
 * @since 04.07.17 - 17:06
 */
public class SwitchOffUsingConnectionFunctionEventData extends ConnectionStrategyChangeEventData {

    private final long oldConnectionFunctionId;

    public SwitchOffUsingConnectionFunctionEventData(ComTaskEnablement comTaskEnablement, ConnectionFunction oldConnectionFunction) {
        super(comTaskEnablement.getId());
        this.oldConnectionFunctionId = oldConnectionFunction.getId();
    }

    @Override
    protected ConnectionStrategyEventType getEventType() {
        return ConnectionStrategyEventType.COMTASKENABLEMENT_SWITCH_OFF_CONNECTION_FUNCTION;
    }

    public long getOldConnectionFunctionId() {
        return oldConnectionFunctionId;
    }
}
