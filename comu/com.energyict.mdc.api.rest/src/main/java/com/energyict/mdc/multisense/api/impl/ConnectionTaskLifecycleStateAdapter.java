/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.multisense.api.impl.utils.TranslationSeeds;

public class ConnectionTaskLifecycleStateAdapter extends MapBasedXmlAdapter<ConnectionTask.ConnectionTaskLifecycleStatus> {

    public ConnectionTaskLifecycleStateAdapter() {
        register(TranslationSeeds.CONNECTION_TASK_STATUS_INCOMPLETE.getKey(), ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE);
        register(TranslationSeeds.CONNECTION_TASK_STATUS_ACTIVE.getKey(), ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
        register(TranslationSeeds.CONNECTION_TASK_STATUS_INACTIVE.getKey(), ConnectionTask.ConnectionTaskLifecycleStatus.INACTIVE);
    }
}
