/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.common.tasks.ConnectionTask;

public class ConnectionTaskLifecycleStateAdapter extends MapBasedXmlAdapter<ConnectionTask.ConnectionTaskLifecycleStatus> {

    public ConnectionTaskLifecycleStateAdapter() {
        register("", null);
        register(DefaultTranslationKey.CONNECTION_TASK_STATUS_INCOMPLETE.getKey(), ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE);
        register(DefaultTranslationKey.CONNECTION_TASK_STATUS_ACTIVE.getKey(), ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
        register(DefaultTranslationKey.CONNECTION_TASK_STATUS_INACTIVE.getKey(), ConnectionTask.ConnectionTaskLifecycleStatus.INACTIVE);
    }

}