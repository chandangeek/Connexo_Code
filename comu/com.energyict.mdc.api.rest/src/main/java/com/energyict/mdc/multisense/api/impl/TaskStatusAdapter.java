/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.device.data.tasks.TaskStatus;

/**
 * Created by bvn on 10/6/15.
 */
public class TaskStatusAdapter extends MapBasedXmlAdapter<TaskStatus> {
    public TaskStatusAdapter() {
        register("Busy", TaskStatus.Busy);
        register("Failed", TaskStatus.Failed);
        register("NeverCompleted", TaskStatus.NeverCompleted);
        register("OnHold", TaskStatus.OnHold);
        register("Pending", TaskStatus.Pending);
        register("Retrying", TaskStatus.Retrying);
        register("Waiting", TaskStatus.Waiting);
        register("ProcessingError", TaskStatus.ProcessingError);
    }
}
