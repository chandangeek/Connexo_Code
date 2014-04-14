package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialOutboundConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import javax.ws.rs.core.UriInfo;

public class ConnectionMethodInfoFactory {
    public static ConnectionMethodInfo asInfo(PartialConnectionTask partialConnectionTask, UriInfo uriInfo) {
        if (PartialInboundConnectionTask.class.isAssignableFrom(partialConnectionTask.getClass())) {
            return new InboundConnectionMethodInfo((PartialInboundConnectionTask) partialConnectionTask, uriInfo);
        } else if (PartialOutboundConnectionTask.class.isAssignableFrom(partialConnectionTask.getClass())) {
            return new OutboundConnectionMethodInfo((PartialOutboundConnectionTask) partialConnectionTask, uriInfo);
        } else if (PartialScheduledConnectionTask.class.isAssignableFrom(partialConnectionTask.getClass())) {
            return new ScheduledConnectionMethodInfo((PartialScheduledConnectionTask) partialConnectionTask, uriInfo);
        } else {
            throw new IllegalArgumentException("Unsupported ConnectionMethod type "+partialConnectionTask.getClass().getSimpleName());
        }
    }
}
