package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTaskBuilder;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
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
    public PartialConnectionTask createPartialTask(DeviceConfiguration deviceConfiguration, EngineModelService engineModelService, ProtocolPluggableService protocolPluggableService) {
        ConnectionTypePluggableClass connectionTypePluggableClass = findConnectionTypeOrThrowException(this.connectionType, protocolPluggableService);
        PartialScheduledConnectionTaskBuilder scheduledConnectionTaskBuilder = deviceConfiguration.createPartialScheduledConnectionTask()
            .name(this.name)
            .pluggableClass(connectionTypePluggableClass)
            .comPortPool((OutboundComPortPool) engineModelService.findComPortPool(this.comPortPool))
            .comWindow(new ComWindow(this.comWindowStart, this.comWindowEnd))
            .asDefault(this.isDefault)
            .connectionStrategy(this.connectionStrategy)
            .rescheduleDelay(this.rescheduleDelay)
            .allowSimultaneousConnections(this.allowSimultaneousConnections);

        addPropertiesToPartialConnectionTask(scheduledConnectionTaskBuilder, connectionTypePluggableClass);
        return scheduledConnectionTaskBuilder.build();
    }

}
