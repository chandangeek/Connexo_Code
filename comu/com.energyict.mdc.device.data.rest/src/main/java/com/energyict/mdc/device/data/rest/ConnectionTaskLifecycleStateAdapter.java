package com.energyict.mdc.device.data.rest;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

/**
 * Copyrights EnergyICT
 * Date: 7/1/14
 * Time: 9:40 AM
 */
public class ConnectionTaskLifecycleStateAdapter extends MapBasedXmlAdapter<ConnectionTask.ConnectionTaskLifecycleState> {

    public ConnectionTaskLifecycleStateAdapter() {
        register("", null);
        register(MessageSeeds.CONNECTION_TASK_STATUS_INCOMPLETE.getKey(), ConnectionTask.ConnectionTaskLifecycleState.INCOMPLETE);
        register(MessageSeeds.CONNECTION_TASK_STATUS_ACTIVE.getKey(), ConnectionTask.ConnectionTaskLifecycleState.ACTIVE);
        register(MessageSeeds.CONNECTION_TASK_STATUS_INACTIVE.getKey(), ConnectionTask.ConnectionTaskLifecycleState.INACTIVE);
    }
}
