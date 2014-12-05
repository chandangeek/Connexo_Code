package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * Listens for create events of {@link ComTaskExecution}s that are marked
 * to use the default {@link ConnectionTask} of the related {@link com.energyict.mdc.device.data.Device}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-05 (14:28)
 */
@Component(name="com.energyict.mdc.device.topology.create.comtaskexecution", service = TopicHandler.class, immediate = true)
@SuppressWarnings("unused")
public class ComTaskExecutionCreateEventHandler implements TopicHandler {

    public static final Logger LOGGER = Logger.getLogger(ComTaskExecutionCreateEventHandler.class.getName());

    private volatile ServerTopologyService topologyService;

    @Override
    public String getTopicMatcher() {
        return "com/energyict/mdc/device/data/comtaskexecution/CREATED";
    }

    @Override
    public void handle(LocalEvent localEvent) {
        ComTaskExecution comTaskExecution = (ComTaskExecution) localEvent.getSource();
        if (comTaskExecution.useDefaultConnectionTask()) {
            this.handle(comTaskExecution);
        }
        else {
            LOGGER.fine("Ignoring creation event since the ComTaskExecution is not configured to use the default");
        }
    }

    private void handle(ComTaskExecution comTaskExecution) {
        Optional<ConnectionTask> defaultConnectionTask = this.findDefaultConnectionTaskForTopology(comTaskExecution);
        defaultConnectionTask.ifPresent(dct -> comTaskExecution.getUpdater().useDefaultConnectionTask(dct));
    }

    private Optional<ConnectionTask> findDefaultConnectionTaskForTopology(ComTaskExecution comTaskExecution) {
        return this.topologyService.findDefaultConnectionTaskForTopology(comTaskExecution.getDevice());
    }

    @Reference
    @SuppressWarnings("unused")
    public void setTopologyService(ServerTopologyService topologyService) {
        this.topologyService = topologyService;
    }

}