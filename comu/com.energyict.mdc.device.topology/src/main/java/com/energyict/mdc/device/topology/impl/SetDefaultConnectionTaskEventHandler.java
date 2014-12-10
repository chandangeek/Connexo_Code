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
 * Listens for events of {@link ConnectionTask}s against
 * master {@link com.energyict.mdc.device.data.Device}s
 * that are marked to be the new default and will set that
 * default ConnectionTask on all {@link ComTaskExecution}s
 * that relate to the master's slave Devices.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-05 (14:28)
 */
@Component(name="com.energyict.mdc.device.topology.set.default.connectiontask", service = TopicHandler.class, immediate = true)
@SuppressWarnings("unused")
public class SetDefaultConnectionTaskEventHandler implements TopicHandler {

    public static final Logger LOGGER = Logger.getLogger(SetDefaultConnectionTaskEventHandler.class.getName());

    private volatile ServerTopologyService topologyService;

    // For OSGi purposes
    public SetDefaultConnectionTaskEventHandler() {
        super();
    }

    // For unit testing purposes
    SetDefaultConnectionTaskEventHandler(ServerTopologyService topologyService) {
        this();
        this.setTopologyService(topologyService);
    }

    @Override
    public String getTopicMatcher() {
        return "com/energyict/mdc/device/data/connectiontask/SETASDEFAULT";
    }

    @Override
    public void handle(LocalEvent localEvent) {
        ConnectionTask<?, ?> connectionTask = (ConnectionTask<?, ?>) localEvent.getSource();
        if (connectionTask instanceof ScheduledConnectionTask) {
            ScheduledConnectionTask scheduledConnectionTask = (ScheduledConnectionTask) connectionTask;
            this.handle(scheduledConnectionTask);
        }
        else {
            LOGGER.fine(() -> "Ignoring update event since it is not for a ScheduledConnectionTask but for a" + connectionTask.getClass().getName());
        }
    }

    private void handle(ScheduledConnectionTask scheduledConnectionTask) {
        if (scheduledConnectionTask.isDefault()) {
            this.topologyService.setOrUpdateDefaultConnectionTaskOnComTasksInDeviceTopology(scheduledConnectionTask.getDevice(), scheduledConnectionTask);
        }
        else {
            LOGGER.fine("Ignoring update event since the ScheduledConnectionTask is not the default");
        }
    }

    @Reference
    @SuppressWarnings("unused")
    public void setTopologyService(ServerTopologyService topologyService) {
        this.topologyService = topologyService;
    }

}