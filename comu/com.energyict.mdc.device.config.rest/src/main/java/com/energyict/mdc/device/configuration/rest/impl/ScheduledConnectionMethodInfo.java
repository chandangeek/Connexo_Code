package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTaskBuilder;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import javax.ws.rs.core.UriInfo;

public class ScheduledConnectionMethodInfo extends ConnectionMethodInfo {

    public ScheduledConnectionMethodInfo() {
    }

    public ScheduledConnectionMethodInfo(PartialScheduledConnectionTask partialConnectionTask, UriInfo uriInfo) {
        super(partialConnectionTask, uriInfo);
        this.connectionStrategy=partialConnectionTask.getConnectionStrategy();
        this.allowSimultaneousConnections=partialConnectionTask.isSimultaneousConnectionsAllowed();
        this.rescheduleDelay=partialConnectionTask.getRescheduleDelay();
    }

    @Override
    public PartialConnectionTask asTask(DeviceConfiguration deviceConfiguration, EngineModelService engineModelService, ProtocolPluggableService protocolPluggableService) {
        PartialScheduledConnectionTaskBuilder scheduledConnectionTaskBuilder = deviceConfiguration.createPartialScheduledConnectionTask()
            .name(this.name)
            .pluggableClass(findConnectionTypeOrThrowException(this.connectionType, protocolPluggableService))
            .comPortPool((OutboundComPortPool) engineModelService.findComPortPool(this.comPortPool))
            .asDefault(this.isDefault)
            .connectionStrategy(this.connectionStrategy)
            .allowSimultaneousConnections(this.allowSimultaneousConnections);

        addPropertiesToPartialConnectionTask(scheduledConnectionTaskBuilder);
        return scheduledConnectionTaskBuilder.build();
    }

}
