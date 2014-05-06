package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.PartialScheduledConnectionTaskBuilder;
import com.energyict.mdc.scheduling.SchedulingService;

/**
 * Copyrights EnergyICT
 * Date: 13/03/14
 * Time: 15:31
 */
public class PartialScheduledConnectionTaskBuilderImpl extends AbstractScheduledPartialConnectionTaskBuilder<PartialScheduledConnectionTaskBuilder, PartialScheduledConnectionTaskImpl> implements PartialScheduledConnectionTaskBuilder {

    private ComWindow comWindow;
    private ConnectionStrategy connectionStrategy;
    private boolean allowSimultaneousConnections;
    private PartialConnectionInitiationTaskImpl partialConnectionInitiationTask;


    PartialScheduledConnectionTaskBuilderImpl(DataModel dataModel, DeviceCommunicationConfigurationImpl configuration, SchedulingService schedulingService, EventService eventService) {
        super(PartialScheduledConnectionTaskBuilder.class, dataModel, configuration, schedulingService, eventService);
    }

    @Override
    public PartialScheduledConnectionTaskBuilder comWindow(ComWindow communicationWindow) {
        this.comWindow = communicationWindow;
        return myself;
    }

    @Override
    public PartialScheduledConnectionTaskBuilder connectionStrategy(ConnectionStrategy connectionStrategy) {
        this.connectionStrategy = connectionStrategy;
        return myself;
    }

    @Override
    public PartialScheduledConnectionTaskBuilder allowSimultaneousConnections(boolean simultaneousConnectionsAllowed) {
        this.allowSimultaneousConnections = simultaneousConnectionsAllowed;
        return myself;
    }

    @Override
    public PartialScheduledConnectionTaskBuilder initiationTask(PartialConnectionInitiationTaskImpl connectionInitiationTask) {
        this.partialConnectionInitiationTask = connectionInitiationTask;
        return myself;
    }

    @Override
    public PartialScheduledConnectionTaskBuilder asDefault(boolean asDefault) {
        this.asDefault = asDefault;
        return myself;
    }

    @Override
    PartialScheduledConnectionTaskImpl newInstance() {
        return PartialScheduledConnectionTaskImpl.from(dataModel, configuration);
    }

    @Override
    void populate(PartialScheduledConnectionTaskImpl instance) {
        super.populate(instance);
        instance.setDefault(asDefault);
        if (comWindow != null) {
            instance.setComWindow(comWindow);
        }
        instance.setConnectionStrategy(connectionStrategy);
        instance.setAllowSimultaneousConnections(allowSimultaneousConnections);
        instance.setInitiationTask(partialConnectionInitiationTask);
    }
}
