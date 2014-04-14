package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import javax.ws.rs.core.UriInfo;

public class InboundConnectionMethodInfo extends ConnectionMethodInfo {

    public InboundConnectionMethodInfo() {
        this.direction="Inbound";
    }

    public InboundConnectionMethodInfo(PartialInboundConnectionTask partialInboundConnectionTask, UriInfo uriInfo) {
        super(partialInboundConnectionTask, uriInfo);
    }
}
