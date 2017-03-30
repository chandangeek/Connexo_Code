/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import com.energyict.mdc.tasks.ProtocolTask;

/**
 * Adds behavior to a ProtocolTask that is private
 * to the server side implementation.
 */
public interface ServerProtocolTask extends ProtocolTask {

    public default boolean isFirmwareUpgradeTask(){
        return false;
    }

}
