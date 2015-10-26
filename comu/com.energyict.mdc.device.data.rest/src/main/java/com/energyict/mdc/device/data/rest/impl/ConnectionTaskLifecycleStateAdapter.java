package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

/**
 * Copyrights EnergyICT
 * Date: 7/1/14
 * Time: 9:40 AM
 */
public class ConnectionTaskLifecycleStateAdapter extends MapBasedXmlAdapter<ConnectionTask.ConnectionTaskLifecycleStatus> {

    public ConnectionTaskLifecycleStateAdapter() {
        register("", null);
        register(DefaultTranslationKey.CONNECTION_TASK_STATUS_INCOMPLETE.getKey(), ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE);
        register(DefaultTranslationKey.CONNECTION_TASK_STATUS_ACTIVE.getKey(), ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
        register(DefaultTranslationKey.CONNECTION_TASK_STATUS_INACTIVE.getKey(), ConnectionTask.ConnectionTaskLifecycleStatus.INACTIVE);
    }

}