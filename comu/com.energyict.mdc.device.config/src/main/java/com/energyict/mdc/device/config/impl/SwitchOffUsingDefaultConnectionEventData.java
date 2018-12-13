/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.PartialConnectionTask;

/**
 * Models the data that is involved in event that are produced when
 * a {@link com.energyict.mdc.device.config.ComTaskEnablement} switches
 * off using the default connection task without specifying a specific
 * {@link PartialConnectionTask}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-18 (15:10)
 */
public class SwitchOffUsingDefaultConnectionEventData extends ConnectionStrategyChangeEventData {

    public SwitchOffUsingDefaultConnectionEventData(ComTaskEnablement comTaskEnablement) {
        super(comTaskEnablement.getId());
    }

    @Override
    protected ConnectionStrategyEventType getEventType() {
        return ConnectionStrategyEventType.COMTASKENABLEMENT_SWITCH_OFF_DEFAULT;
    }

}