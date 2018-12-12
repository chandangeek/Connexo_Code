/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.logging.Logger;

/**
 * Listens for events against master {@link com.energyict.mdc.device.data.Device}s
 * whose default ConnectionTask has been cleared,
 * i.e. there is no longer a default and will update all {@link ComTaskExecution}s
 * that relate to the master's slave Devices and that depend on the default.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-05 (14:28)
 */
@Component(name="com.energyict.mdc.device.topology.clear.default.connectiontask", service = TopicHandler.class, immediate = true)
@SuppressWarnings("unused")
public class ClearDefaultConnectionTaskEventHandler implements TopicHandler {

    public static final Logger LOGGER = Logger.getLogger(ClearDefaultConnectionTaskEventHandler.class.getName());

    private volatile ServerTopologyService topologyService;

    // For OSGi purposes
    public ClearDefaultConnectionTaskEventHandler() {
        super();
    }

    // For unit testing purposes
    ClearDefaultConnectionTaskEventHandler(ServerTopologyService topologyService) {
        this.setTopologyService(topologyService);
    }

    @Override
    public String getTopicMatcher() {
        return "com/energyict/mdc/device/data/connectiontask/CLEARDEFAULT";
    }

    @Override
    public void handle(LocalEvent localEvent) {
        Device device = ((ConnectionTask) localEvent.getSource()).getDevice();
        this.topologyService.setOrUpdateDefaultConnectionTaskOnComTasksInDeviceTopology(device, null);
    }

    @Reference
    @SuppressWarnings("unused")
    public void setTopologyService(ServerTopologyService topologyService) {
        this.topologyService = topologyService;
    }

}