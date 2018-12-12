/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

public class ConnectionTaskLifecycleStatusAdapter extends MapBasedXmlAdapter<ConnectionTask.ConnectionTaskLifecycleStatus> {

    public ConnectionTaskLifecycleStatusAdapter() {
        register("active", ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE);
        register("inactive", ConnectionTask.ConnectionTaskLifecycleStatus.INACTIVE);
        register("incomplete", ConnectionTask.ConnectionTaskLifecycleStatus.INCOMPLETE);
    }
}
