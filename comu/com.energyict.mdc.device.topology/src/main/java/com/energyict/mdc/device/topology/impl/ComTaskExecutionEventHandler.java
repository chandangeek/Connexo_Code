package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * Listens for events of {@link ComTaskExecution}s that are marked
 * to use the default {@link ConnectionTask} of the related
 * {@link com.energyict.mdc.device.data.Device}
 * but are not linked to the default connection task.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-05 (14:28)
 */
public abstract class ComTaskExecutionEventHandler implements TopicHandler {

    private static final Logger LOGGER = Logger.getLogger(ComTaskExecutionEventHandler.class.getName());
    private static final long NO_ID = -1;

    private volatile ServerTopologyService topologyService;

    // For OSGi purposes
    public ComTaskExecutionEventHandler() {
        super();
    }

    // For unit testing purposes
    ComTaskExecutionEventHandler(ServerTopologyService topologyService) {
        this();
        this.setTopologyService(topologyService);
    }

    protected abstract void setTopologyService(ServerTopologyService topologyService);

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
        defaultConnectionTask.ifPresent(dct -> this.setDefaultConnectionTask(comTaskExecution, dct));
    }

    private void setDefaultConnectionTask(ComTaskExecution comTaskExecution, ConnectionTask dct) {
        if (different(comTaskExecution.getConnectionTask(), dct)) {
            comTaskExecution.getUpdater().useDefaultConnectionTask(dct).update();
        }
    }

    private boolean different(Optional<ConnectionTask<?, ?>> ct1, ConnectionTask ct2) {
        return !this.sameId(ct1, ct2);
    }

    @SuppressWarnings("ConditionalExpression")
    private boolean sameId(Optional<ConnectionTask<?, ?>> ct1, ConnectionTask ct2) {
        return sameId(ct1.isPresent() ? ct1.get().getId() : NO_ID, ct2 != null ? ct2.getId() : NO_ID);
    }

    private boolean sameId(long id1, long id2) {
        return id1 == id2;
    }

    private Optional<ConnectionTask> findDefaultConnectionTaskForTopology(ComTaskExecution comTaskExecution) {
        return this.topologyService.findDefaultConnectionTaskForTopology(comTaskExecution.getDevice());
    }

    @SuppressWarnings("unused")
    protected void setReferencedTopologyService(ServerTopologyService topologyService) {
        this.topologyService = topologyService;
    }

}