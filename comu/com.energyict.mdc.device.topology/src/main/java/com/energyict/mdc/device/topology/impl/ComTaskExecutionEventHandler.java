/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.ConnectionFunction;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * Listens for events of {@link ComTaskExecution}s that are marked
 * to use either the default {@link ConnectionTask} or either the {@link ConnectionTask}
 * corresponding to a certain {@link ConnectionFunction} of the related
 * {@link Device}
 * but are not linked to the correct default/connection function based connection task.
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
            this.handleWhenUsingDefaultConnectionTask(comTaskExecution);
        } else if (comTaskExecution.getConnectionFunction().isPresent()) {
            this.handleWhenUsingConnectionFunction(comTaskExecution);
        } else {
            LOGGER.fine("Ignoring creation event since the ComTaskExecution is not configured to use the default nor is configured to use a connection function");
        }
    }

    private void handleWhenUsingDefaultConnectionTask(ComTaskExecution comTaskExecution) {
        Optional<ConnectionTask> defaultConnectionTask = this.findDefaultConnectionTaskForTopology(comTaskExecution);
        defaultConnectionTask.ifPresent(dct -> this.setDefaultConnectionTask(comTaskExecution, dct));
    }

    private void setDefaultConnectionTask(ComTaskExecution comTaskExecution, ConnectionTask dct) {
        if (different(comTaskExecution.getConnectionTask(), dct)) {
            LOGGER.info("CXO-11731: Update comtask execution from setDefaultConnectionTask");
            comTaskExecution.getUpdater().useDefaultConnectionTask(dct).update();
        }
    }

    private void handleWhenUsingConnectionFunction(ComTaskExecution comTaskExecution) {
        Optional<ConnectionTask> connectionTask = this.findConnectionTaskWithConnectionFunctionForTopology(comTaskExecution);
        connectionTask.ifPresent(dct -> this.setConnectionTaskHavingConnectionFunction(comTaskExecution, dct));
    }

    private void setConnectionTaskHavingConnectionFunction(ComTaskExecution comTaskExecution, ConnectionTask dct) {
        if (different(comTaskExecution.getConnectionTask(), dct)) {
            LOGGER.info("CXO-11731: Update comtask execution from setConnectionTaskHavingConnectionFunction");
            comTaskExecution.getUpdater().useConnectionTaskBasedOnConnectionFunction(dct).update();
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

    private Optional<ConnectionTask> findConnectionTaskWithConnectionFunctionForTopology(ComTaskExecution comTaskExecution) {
        ConnectionFunction connectionFunction = comTaskExecution.getConnectionFunction().get(); // If we reach this point, the optional should be present (that is already checked before)
        return this.topologyService.findConnectionTaskWithConnectionFunctionForTopology(comTaskExecution.getDevice(), connectionFunction);
    }

    @SuppressWarnings("unused")
    protected void setReferencedTopologyService(ServerTopologyService topologyService) {
        this.topologyService = topologyService;
    }

}