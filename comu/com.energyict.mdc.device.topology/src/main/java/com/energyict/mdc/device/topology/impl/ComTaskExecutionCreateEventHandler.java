/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.events.TopicHandler;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Listens for creation events of {@link ComTaskExecution}s that are marked
 * to use the default {@link ConnectionTask} of the related
 * {@link com.energyict.mdc.device.data.Device}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-05 (14:28)
 */
@Component(name="com.energyict.mdc.device.topology.create.comtaskexecution", service = TopicHandler.class, immediate = true)
@SuppressWarnings("unused")
public class ComTaskExecutionCreateEventHandler extends ComTaskExecutionEventHandler {

    // For OSGi purposes
    public ComTaskExecutionCreateEventHandler() {
        super();
    }

    // For unit testing purposes
    ComTaskExecutionCreateEventHandler(ServerTopologyService topologyService) {
        super(topologyService);
    }

    @Override
    public String getTopicMatcher() {
        return "com/energyict/mdc/device/data/comtaskexecution/CREATED";
    }

    @Reference
    @SuppressWarnings("unused")
    public void setTopologyService(ServerTopologyService topologyService) {
        this.setReferencedTopologyService(topologyService);
    }
}