package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialOutboundConnectionTask;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import javax.ws.rs.core.UriInfo;

public class OutboundConnectionMethodInfo extends ConnectionMethodInfo {

    public OutboundConnectionMethodInfo() {
        this.direction="Outbound";
    }

    public OutboundConnectionMethodInfo(PartialOutboundConnectionTask partialInboundConnectionTask, UriInfo uriInfo) {
        super(partialInboundConnectionTask, uriInfo);
    }

    @Override
    public PartialConnectionTask createPartialTask(DeviceConfiguration deviceConfiguration, EngineModelService engineModelService, ProtocolPluggableService protocolPluggableService) {
        return null;
    }

}
