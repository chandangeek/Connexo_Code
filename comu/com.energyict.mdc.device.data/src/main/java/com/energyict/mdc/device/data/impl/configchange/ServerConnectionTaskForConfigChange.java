/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.configchange;

import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.config.ComPortPool;

/**
 * Provides functionality to manipulate the ConnectionTask in order to perform a Device config change
 */
public interface ServerConnectionTaskForConfigChange<CPPT extends ComPortPool, PCTT extends PartialConnectionTask> extends ConnectionTask<CPPT, PCTT> {

    /**
     * Sets AND updates the new PartialConnectionTask
     *
     * @param partialConnectionTask the new PartialConnectionTask to set
     */
    void setNewPartialConnectionTask(PCTT partialConnectionTask);
}
