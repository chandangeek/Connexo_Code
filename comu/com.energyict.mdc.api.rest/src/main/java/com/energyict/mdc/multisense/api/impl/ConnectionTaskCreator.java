/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.PartialConnectionTask;

/**
 * Created by bvn on 10/6/15.
 */
public interface ConnectionTaskCreator {
    public ConnectionTask<?,?> createTask(ConnectionTaskInfo info, ConnectionTaskInfoFactory factory, Device device, PartialConnectionTask partialConnectionTask);
}
