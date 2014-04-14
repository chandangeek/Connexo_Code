package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import javax.ws.rs.core.UriInfo;

public class ScheduledConnectionMethodInfo extends ConnectionMethodInfo {

    public ScheduledConnectionMethodInfo() {
        this.direction="Scheduled";
    }

    public ScheduledConnectionMethodInfo(PartialScheduledConnectionTask partialConnectionTask, UriInfo uriInfo) {
        super(partialConnectionTask, uriInfo);
        this.connectionStrategy=partialConnectionTask.getConnectionStrategy();
        this.allowSimultaneousConnections=partialConnectionTask.isSimultaneousConnectionsAllowed();
        this.rescheduleDelay=partialConnectionTask.getRescheduleDelay();
    }
}
