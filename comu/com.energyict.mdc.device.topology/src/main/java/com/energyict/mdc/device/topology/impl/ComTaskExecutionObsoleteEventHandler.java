package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.topology.TopologyService;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * Listens for "make obsolete" events of {@link ComTaskExecution}s that are marked
 * to use the default {@link ConnectionTask} of a {@link com.energyict.mdc.device.data.Device} topology
 * that is currently executing because the default ConnectionTask is in use.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-05 (14:28)
 */
@Component(name="com.energyict.mdc.device.topology.makeobsolete.comtaskexecution", service = TopicHandler.class, immediate = true)
@SuppressWarnings("unused")
public class ComTaskExecutionObsoleteEventHandler implements TopicHandler {

    public static final Logger LOGGER = Logger.getLogger(ComTaskExecutionObsoleteEventHandler.class.getName());

    private volatile ServerTopologyService topologyService;
    private volatile Thesaurus thesaurus;

    // For OSGi purposes
    public ComTaskExecutionObsoleteEventHandler() {
        super();
    }

    // For unit testing purposes only
    ComTaskExecutionObsoleteEventHandler(ServerTopologyService topologyService, Thesaurus thesaurus) {
        this();
        this.setTopologyService(topologyService);
        this.thesaurus = thesaurus;
    }

    @Override
    public String getTopicMatcher() {
        return "com/energyict/mdc/device/data/comtaskexecution/VALIDATE_OBSOLETE";
    }

    @Override
    public void handle(LocalEvent localEvent) {
        ComTaskExecution comTaskExecution = (ComTaskExecution) localEvent.getSource();
        if (comTaskExecution.usesDefaultConnectionTask()) {
            this.handle(comTaskExecution);
        }
        else {
            LOGGER.fine("Ignoring creation event since the ComTaskExecution is not configured to use the default");
        }
    }

    private void handle(ComTaskExecution comTaskExecution) {
        Optional<ConnectionTask> defaultConnectionTask = this.findDefaultConnectionTaskForTopology(comTaskExecution);
        defaultConnectionTask
            .map(ConnectionTask::getExecutingComServer)
            .ifPresent(cs -> {
                throw new ComTaskExecutionIsExecutingAndCannotBecomeObsoleteException(
                        comTaskExecution, defaultConnectionTask.get().getExecutingComServer(),
                        this.thesaurus,
                        MessageSeeds.COM_TASK_EXECUTION_IS_EXECUTING_AND_CANNOT_OBSOLETE);
            });
    }

    private Optional<ConnectionTask> findDefaultConnectionTaskForTopology(ComTaskExecution comTaskExecution) {
        return this.topologyService.findDefaultConnectionTaskForTopology(comTaskExecution.getDevice());
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(TopologyService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    @SuppressWarnings("unused")
    public void setTopologyService(ServerTopologyService topologyService) {
        this.topologyService = topologyService;
    }

}