package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTaskBuilder;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import javax.ws.rs.core.UriInfo;

public class InboundConnectionMethodInfo extends ConnectionMethodInfo {

    public InboundConnectionMethodInfo() {
        this.direction="Inbound";
    }

    public InboundConnectionMethodInfo(PartialInboundConnectionTask partialInboundConnectionTask, UriInfo uriInfo) {
        super(partialInboundConnectionTask, uriInfo);
    }

    @Override
    public PartialConnectionTask asTask(DeviceConfiguration deviceConfiguration, EngineModelService engineModelService, ProtocolPluggableService protocolPluggableService) {
        PartialInboundConnectionTaskBuilder connectionTaskBuilder = deviceConfiguration.getCommunicationConfiguration().createPartialInboundConnectionTask()
            .name(this.name)
            .pluggableClass(findConnectionTypeOrThrowException(this.connectionType, protocolPluggableService))
            .comPortPool((InboundComPortPool) engineModelService.findComPortPool(this.comPortPool))
            .asDefault(this.isDefault);
        addPropertiesToPartialConnectionTask(connectionTaskBuilder);
        return connectionTaskBuilder.build();
    }
}
