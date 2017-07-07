/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.protocol.api.ConnectionFunction;

/**
 * Models the data that is involved in event that are produced when
 * a {@link ComTaskEnablement} switches
 * from using a connection function to a specific
 * {@link PartialConnectionTask}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-18 (15:10)
 */
public class SwitchFromDefaultConnectionToConnectionFunctionEventData extends ConnectionStrategyChangeEventData {
    private final long newConnectionFunctionId;

    public SwitchFromDefaultConnectionToConnectionFunctionEventData(ComTaskEnablement comTaskEnablement, ConnectionFunction newConnectionFunction) {
        super(comTaskEnablement.getId());
        this.newConnectionFunctionId = newConnectionFunction.getId();
    }

    @Override
    protected ConnectionStrategyEventType getEventType() {
        return ConnectionStrategyEventType.COMTASKENABLEMENT_SWITCH_FROM_DEFAULT_TO_CONNECTION_FUNCTION;
    }

    public long getNewConnectionFunctionId() {
        return newConnectionFunctionId;
    }
}