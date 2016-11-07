package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Listens for create events of {@link ConnectionTask}s against
 * master {@link com.energyict.mdc.device.data.Device}s; in case the {@link ConnectionTask}
 * was marked as the default one, it will be set on all
 * {@link ComTaskExecution}s that relate to the master's slave Devices.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-05 (14:28)
 */
@Component(name = "com.energyict.mdc.device.topology.create.default.connectiontask", service = TopicHandler.class, immediate = true)
@SuppressWarnings("unused")
public class DefaultConnectionTaskCreateEventHandler implements TopicHandler {

    public static final Logger LOGGER = Logger.getLogger(DefaultConnectionTaskCreateEventHandler.class.getName());

    private volatile ServerTopologyService topologyService;

    // For OSGi purposes
    public DefaultConnectionTaskCreateEventHandler() {
        super();
    }

    // For unit testing purposes
    public DefaultConnectionTaskCreateEventHandler(ServerTopologyService topologyService) {
        this();
        this.topologyService = topologyService;
    }

    @Override
    public String getTopicMatcher() {
        return "com/energyict/mdc/device/data/connectiontask/CREATED";
    }

    @Override
    public void handle(LocalEvent localEvent) {
        ConnectionTask<?, ?> connectionTask = (ConnectionTask<?, ?>) localEvent.getSource();
        this.handle(connectionTask);
    }

    private void handle(ConnectionTask connectionTask) {
        if (connectionTask.isDefault()) {
            this.topologyService.setOrUpdateDefaultConnectionTaskOnComTasksInDeviceTopology(connectionTask.getDevice(), connectionTask);
        } else {
            connectionTask.getDevice().setConnectionTaskForComTaskExecutions(connectionTask);
        }
    }

    @Reference
    @SuppressWarnings("unused")
    public void setTopologyService(ServerTopologyService topologyService) {
        this.topologyService = topologyService;
    }

}