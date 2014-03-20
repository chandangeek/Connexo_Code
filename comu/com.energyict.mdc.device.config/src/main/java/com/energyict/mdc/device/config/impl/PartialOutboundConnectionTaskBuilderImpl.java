package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.PartialOutboundConnectionTaskBuilder;

/**
 * Copyrights EnergyICT
 * Date: 13/03/14
 * Time: 15:31
 */
public class PartialOutboundConnectionTaskBuilderImpl extends AbstractScheduledPartialConnectionTaskBuilder<PartialOutboundConnectionTaskBuilder, PartialOutboundConnectionTask> implements PartialOutboundConnectionTaskBuilder {

    private ComWindow comWindow;
    private ConnectionStrategy connectionStrategy;
    private boolean allowSimultaneousConnections;
    private PartialConnectionInitiationTask partialConnectionInitiationTask;


    PartialOutboundConnectionTaskBuilderImpl(DataModel dataModel, DeviceCommunicationConfiguration configuration) {
        super(PartialOutboundConnectionTaskBuilder.class, dataModel, configuration);
    }

    @Override
    public PartialOutboundConnectionTaskBuilder comWindow(ComWindow communicationWindow) {
        this.comWindow = communicationWindow;
        return myself;
    }

    @Override
    public PartialOutboundConnectionTaskBuilder connectionStrategy(ConnectionStrategy connectionStrategy) {
        this.connectionStrategy = connectionStrategy;
        return myself;
    }

    @Override
    public PartialOutboundConnectionTaskBuilder allowSimultaneousConnections(boolean simultaneousConnectionsAllowed) {
        this.allowSimultaneousConnections = simultaneousConnectionsAllowed;
        return myself;
    }

    @Override
    public PartialOutboundConnectionTaskBuilder initiatonTask(PartialConnectionInitiationTask connectionInitiationTask) {
        this.partialConnectionInitiationTask = connectionInitiationTask;
        return myself;
    }

    @Override
    PartialOutboundConnectionTask newInstance() {
        return PartialOutboundConnectionTaskImpl.from(dataModel, configuration);
    }

    @Override
    void populate(PartialOutboundConnectionTask instance) {
        super.populate(instance);
        instance.setComWindow(comWindow);
        instance.setConnectionStrategy(connectionStrategy);
        instance.setAllowSimultaneousConnections(allowSimultaneousConnections);
        instance.setInitiationTask(partialConnectionInitiationTask);
    }
}
