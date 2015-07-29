package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;

import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;

public class ConnectionMethodInfoFactory {

    private final MdcPropertyUtils mdcPropertyUtils;

    @Inject
    public ConnectionMethodInfoFactory(MdcPropertyUtils mdcPropertyUtils) {
        this.mdcPropertyUtils = mdcPropertyUtils;
    }

    public ConnectionMethodInfo<?> asInfo(PartialConnectionTask partialConnectionTask, UriInfo uriInfo) {
        if (PartialInboundConnectionTask.class.isAssignableFrom(partialConnectionTask.getClass())) {
            return new InboundConnectionMethodInfo((PartialInboundConnectionTask) partialConnectionTask, uriInfo, mdcPropertyUtils);
        } else if (PartialScheduledConnectionTask.class.isAssignableFrom(partialConnectionTask.getClass())) {
            return new ScheduledConnectionMethodInfo((PartialScheduledConnectionTask) partialConnectionTask, uriInfo, mdcPropertyUtils);
        } else {
            throw new IllegalArgumentException("Unsupported ConnectionMethod type "+partialConnectionTask.getClass().getSimpleName());
        }
    }
}
