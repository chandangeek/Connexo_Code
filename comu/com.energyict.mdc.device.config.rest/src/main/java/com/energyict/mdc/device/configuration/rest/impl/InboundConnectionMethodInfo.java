package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTaskBuilder;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import javax.ws.rs.core.UriInfo;

public class InboundConnectionMethodInfo extends ConnectionMethodInfo<PartialInboundConnectionTask> {

    public InboundConnectionMethodInfo() {
    }

    public InboundConnectionMethodInfo(PartialInboundConnectionTask partialInboundConnectionTask, UriInfo uriInfo) {
        super(partialInboundConnectionTask, uriInfo);
    }

    @Override
    protected void writeTo(PartialInboundConnectionTask partialConnectionTask, EngineModelService engineModelService) {
        super.writeTo(partialConnectionTask, engineModelService);
        partialConnectionTask.setComportPool(Checks.is(this.comPortPool).emptyOrOnlyWhiteSpace()?null:(InboundComPortPool) engineModelService.findComPortPool(this.comPortPool));
        partialConnectionTask.setDefault(this.isDefault);
    }

    @Override
    public PartialConnectionTask createPartialTask(DeviceConfiguration deviceConfiguration, EngineModelService engineModelService, ProtocolPluggableService protocolPluggableService) {
        ConnectionTypePluggableClass connectionTypePluggableClass = findConnectionTypeOrThrowException(this.connectionType, protocolPluggableService);
        PartialInboundConnectionTaskBuilder connectionTaskBuilder = deviceConfiguration.getCommunicationConfiguration().createPartialInboundConnectionTask()
            .name(this.name)
            .pluggableClass(connectionTypePluggableClass)
            .comPortPool((InboundComPortPool) engineModelService.findComPortPool(this.comPortPool))
            .asDefault(this.isDefault);
        addPropertiesToPartialConnectionTask(connectionTaskBuilder, connectionTypePluggableClass);
        return connectionTaskBuilder.build();
    }
}
