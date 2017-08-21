/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.protocol.api.ConnectionFunction;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.logging.Logger;

/**
 * Listens for events of {@link ConnectionTask}s whose {@link ConnectionFunction} has been removed,
 * i.e. the ConnectionTask no longer acts as the one having the given ConnectionFunction.
 * All {@link ComTaskExecution}s that relate to the master's slave Devices and that rely on the given
 * ConnectionFunction should be updated (by recalculating their {@link ConnectionTask}).
 *
 * @author Stijn Vanhoorelbeke
 * @since 2017-07-07 (14:28)
 */
@Component(name = "com.energyict.mdc.device.topology.clear.connectionfunction.on.connectiontask", service = TopicHandler.class, immediate = true)
@SuppressWarnings("unused")
public class RecalculateConnectionFunctionOnConnectionTaskEventHandler implements TopicHandler {

    public static final Logger LOGGER = Logger.getLogger(RecalculateConnectionFunctionOnConnectionTaskEventHandler.class.getName());

    private volatile ServerTopologyService topologyService;

    // For OSGi purposes
    public RecalculateConnectionFunctionOnConnectionTaskEventHandler() {
        super();
    }

    // For unit testing purposes
    RecalculateConnectionFunctionOnConnectionTaskEventHandler(ServerTopologyService topologyService) {
        this();
        this.setTopologyService(topologyService);
    }

    @Override
    public String getTopicMatcher() {
        return "com/energyict/mdc/device/data/connectiontask/CLEARFUNCTION";
    }

    @Override
    public void handle(LocalEvent localEvent) {
        Pair<ConnectionTask<?, ?>, ConnectionFunction> pair = (Pair<ConnectionTask<?, ?>, ConnectionFunction>) localEvent.getSource();
        this.handle(pair.getFirst(), pair.getLast());
    }

    private void handle(ConnectionTask<?, ?> connectionTask, ConnectionFunction oldConnectionFunction) {
        this.topologyService.recalculateConnectionTaskHavingConnectionFunctionOnComTasksInDeviceTopology(connectionTask.getDevice(), oldConnectionFunction);
    }

    @Reference
    @SuppressWarnings("unused")
    public void setTopologyService(ServerTopologyService topologyService) {
        this.topologyService = topologyService;
    }
}