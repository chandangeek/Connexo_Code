package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.logging.Logger;

/**
 * Listens for create events of {@link ConnectionTask}s against
 * master {@link com.energyict.mdc.device.data.Device}s that are marked as the default
 * and will set that default ConnectionTask on all
 * {@link ComTaskExecution}s that relate to the master's slave Devices.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-05 (14:28)
 */
@Component(name="com.energyict.mdc.device.topology.create.default.connectiontask", service = TopicHandler.class, immediate = true)
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
        if (connectionTask instanceof ScheduledConnectionTask) {
            ScheduledConnectionTask scheduledConnectionTask = (ScheduledConnectionTask) connectionTask;
            this.handle(scheduledConnectionTask);
        }
        else {
            LOGGER.fine(() -> "Ignoring creation event since it is not for a ScheduledConnectionTask but for a" + connectionTask.getClass().getName());
        }
    }

    private void handle(ScheduledConnectionTask scheduledConnectionTask) {
        this.topologyService.setOrUpdateDefaultConnectionTaskOnComTasksInDeviceTopology(scheduledConnectionTask.getDevice(), scheduledConnectionTask);
    }

    @Reference
    @SuppressWarnings("unused")
    public void setTopologyService(ServerTopologyService topologyService) {
        this.topologyService = topologyService;
    }

}