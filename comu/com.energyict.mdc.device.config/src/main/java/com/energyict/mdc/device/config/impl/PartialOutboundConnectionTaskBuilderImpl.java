package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.PartialOutboundConnectionTaskBuilder;

/**
 * Copyrights EnergyICT
 * Date: 13/03/14
 * Time: 15:31
 */
public class PartialOutboundConnectionTaskBuilderImpl extends AbstractScheduledPartialConnectionTaskBuilder<PartialOutboundConnectionTaskBuilder, ServerPartialOutboundConnectionTask> implements PartialOutboundConnectionTaskBuilder {

    private ComWindow comWindow;
    private ConnectionStrategy connectionStrategy;
    private boolean allowSimultaneousConnections;
    private PartialConnectionInitiationTaskImpl partialConnectionInitiationTask;


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
    public PartialOutboundConnectionTaskBuilder initiatonTask(PartialConnectionInitiationTaskImpl connectionInitiationTask) {
        this.partialConnectionInitiationTask = connectionInitiationTask;
        return myself;
    }

    @Override
    public PartialOutboundConnectionTaskBuilder asDefault(boolean asDefault) {
        this.asDefault = asDefault;
        return myself;
    }

    @Override
    ServerPartialOutboundConnectionTask newInstance() {
        return PartialOutboundConnectionTaskImpl.from(dataModel, configuration);
    }

    @Override
    void populate(ServerPartialOutboundConnectionTask instance) {
        super.populate(instance);
        instance.setDefault(asDefault);
        instance.setComWindow(comWindow);
        instance.setConnectionStrategy(connectionStrategy);
        instance.setAllowSimultaneousConnections(allowSimultaneousConnections);
        instance.setInitiationTask(partialConnectionInitiationTask);
    }
}
