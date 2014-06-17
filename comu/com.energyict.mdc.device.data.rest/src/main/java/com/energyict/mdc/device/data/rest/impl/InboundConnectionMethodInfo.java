package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import javax.ws.rs.core.UriInfo;

public class InboundConnectionMethodInfo extends ConnectionMethodInfo<InboundConnectionTask> {

    public InboundConnectionMethodInfo() {
    }

    public InboundConnectionMethodInfo(InboundConnectionTask partialInboundConnectionTask, UriInfo uriInfo, MdcPropertyUtils mdcPropertyUtils) {
        super(partialInboundConnectionTask, uriInfo, mdcPropertyUtils);
    }

    @Override
    protected void writeTo(InboundConnectionTask connectionTask, EngineModelService engineModelService) {
        super.writeTo(connectionTask, engineModelService);
//        connectionTask.setComportPool(Checks.is(this.comPortPool).emptyOrOnlyWhiteSpace()?null:(InboundComPortPool) engineModelService.findComPortPool(this.comPortPool));
//        connectionTask.setDefault(this.isDefault);
    }

    @Override
    public ConnectionTask<?,?> createTask(DeviceConfiguration deviceConfiguration, EngineModelService engineModelService, ProtocolPluggableService protocolPluggableService, MdcPropertyUtils mdcPropertyUtils) {
//        this.mdcPropertyUtils = mdcPropertyUtils;
//        ConnectionTypePluggableClass connectionTypePluggableClass = findConnectionTypeOrThrowException(this.connectionType, protocolPluggableService);
//        PartialInboundConnectionTaskBuilder connectionTaskBuilder = deviceConfiguration.getCommunicationConfiguration().newPartialInboundConnectionTask(name, connectionTypePluggableClass)
//            .comPortPool((InboundComPortPool) engineModelService.findComPortPool(this.comPortPool))
//            .asDefault(this.isDefault);
//        addPropertiesToPartialConnectionTask(connectionTaskBuilder, connectionTypePluggableClass);
//        return connectionTaskBuilder.build();
        return null;
    }
}
