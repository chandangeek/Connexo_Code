/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.protocol.api.ConnectionFunction;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.logging.Logger;

/**
 * Listens for events of {@link ConnectionTask}s against
 * master {@link Device}s; in case the {@link ConnectionTask} is marked
 * to use a specific {@link ConnectionFunction},
 * it will be set on all {@link ComTaskExecution}s requiring the same {@link ConnectionFunction}
 * that relate to the master's slave Devices.
 *
 * @author Stijn Vanhoorelbeke
 * @since 2017-07-07 (14:28)
 */
@Component(name="com.energyict.mdc.device.topology.set.connectionfunction.on.connectiontask", service = TopicHandler.class, immediate = true)
@SuppressWarnings("unused")
public class SetConnectionFunctionOnConnectionTaskEventHandler implements TopicHandler {

    public static final Logger LOGGER = Logger.getLogger(SetConnectionFunctionOnConnectionTaskEventHandler.class.getName());

    private volatile ServerTopologyService topologyService;

    // For OSGi purposes
    public SetConnectionFunctionOnConnectionTaskEventHandler() {
        super();
    }

    // For unit testing purposes
    SetConnectionFunctionOnConnectionTaskEventHandler(ServerTopologyService topologyService) {
        this();
        this.setTopologyService(topologyService);
    }

    @Override
    public String getTopicMatcher() {
        return "com/energyict/mdc/device/data/connectiontask/SETASFUNCTION";
    }

    @Override
    public void handle(LocalEvent localEvent) {
        ConnectionTask<?, ?> connectionTask = (ConnectionTask<?, ?>) localEvent.getSource();
        this.handle(connectionTask);
    }

    private void handle(ConnectionTask connectionTask) {
        if (connectionTask.getPartialConnectionTask().getConnectionFunction().isPresent()) {
            this.topologyService.setOrUpdateConnectionTaskHavingConnectionFunctionOnComTasksInDeviceTopology(connectionTask.getDevice(), connectionTask);
        } else {
            LOGGER.fine("Ignoring update event since the ConnectionTask does not use a connection function");
        }
    }

    @Reference
    @SuppressWarnings("unused")
    public void setTopologyService(ServerTopologyService topologyService) {
        this.topologyService = topologyService;
    }
}