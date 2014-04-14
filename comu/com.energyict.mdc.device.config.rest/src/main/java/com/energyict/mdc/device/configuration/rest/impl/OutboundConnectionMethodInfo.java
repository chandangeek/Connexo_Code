package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.config.PartialOutboundConnectionTask;
import javax.ws.rs.core.UriInfo;

public class OutboundConnectionMethodInfo extends ConnectionMethodInfo {

    public OutboundConnectionMethodInfo() {
        this.direction="Outbound";
    }

    public OutboundConnectionMethodInfo(PartialOutboundConnectionTask partialInboundConnectionTask, UriInfo uriInfo) {
        super(partialInboundConnectionTask, uriInfo);
    }
}
