/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

/**
 * Created by bvn on 10/6/15.
 */
public interface ConnectionTaskUpdater {
    public ConnectionTask<?,?> updateTask(long connectionTaskId, ConnectionTaskInfo info, ConnectionTaskInfoFactory factory, Device device, ConnectionTask<?, ?> connectionTask);
}
