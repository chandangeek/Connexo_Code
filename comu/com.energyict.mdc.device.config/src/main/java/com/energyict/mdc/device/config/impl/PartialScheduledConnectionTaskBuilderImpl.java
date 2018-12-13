/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.PartialScheduledConnectionTaskBuilder;
import com.energyict.mdc.scheduling.SchedulingService;

class PartialScheduledConnectionTaskBuilderImpl extends AbstractScheduledPartialConnectionTaskBuilder<PartialScheduledConnectionTaskBuilder, PartialScheduledConnectionTaskImpl> implements PartialScheduledConnectionTaskBuilder {

    private ComWindow comWindow;
    private ConnectionStrategy connectionStrategy;
    private int numberOfSimultaneousConnections = 1;
    private PartialConnectionInitiationTaskImpl partialConnectionInitiationTask;


    PartialScheduledConnectionTaskBuilderImpl(DataModel dataModel, DeviceConfigurationImpl configuration, SchedulingService schedulingService, EventService eventService) {
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
    public PartialScheduledConnectionTaskBuilder setNumberOfSimultaneousConnections(int numberOfSimultaneousConnections) {
        this.numberOfSimultaneousConnections = numberOfSimultaneousConnections;
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
        instance.setNumberOfSimultaneousConnections(numberOfSimultaneousConnections);
        instance.setInitiationTask(partialConnectionInitiationTask);
    }
}
