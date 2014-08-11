package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

/**
 * Created by bvn on 8/11/14.
 */
public class ConnectionTaskLifecycleStatusAdaptor extends MapBasedXmlAdapter<ConnectionTask.ConnectionTaskLifecycleStatus> {

    public ConnectionTaskLifecycleStatusAdaptor() {
        register("active", ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
        register("inactive", ConnectionTask.ConnectionTaskLifecycleStatus.INACTIVE);
        register("incomplete", ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE);
    }
}
