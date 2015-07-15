package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

/**
 * Copyrights EnergyICT
 * Date: 7/1/14
 * Time: 9:40 AM
 */
public class ConnectionTaskLifecycleStateAdapter extends MapBasedXmlAdapter<ConnectionTask.ConnectionTaskLifecycleStatus> {

    public ConnectionTaskLifecycleStateAdapter() {
        register(TranslationSeeds.CONNECTION_TASK_STATUS_INCOMPLETE.getKey(), ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE);
        register(TranslationSeeds.CONNECTION_TASK_STATUS_ACTIVE.getKey(), ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
        register(TranslationSeeds.CONNECTION_TASK_STATUS_INACTIVE.getKey(), ConnectionTask.ConnectionTaskLifecycleStatus.INACTIVE);
    }
}
