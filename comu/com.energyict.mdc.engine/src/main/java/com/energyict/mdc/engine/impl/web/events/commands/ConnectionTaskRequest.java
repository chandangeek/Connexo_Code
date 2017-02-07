/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.engine.impl.events.EventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.singleton;

/**
 * Provides an implementation for the {@link Request} interface
 * that represents a request to register interest
 * in events that relate to a single {@link ConnectionTask}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-05 (09:59)
 */
class ConnectionTaskRequest extends IdBusinessObjectRequest {

    private final ConnectionTaskService connectionTaskService;
    private List<ConnectionTask> connectionTasks;

    ConnectionTaskRequest(ConnectionTaskService connectionTaskService, long connectionTaskId) {
        this(connectionTaskService, singleton(connectionTaskId));
    }

    ConnectionTaskRequest(ConnectionTaskService connectionTaskService, Set<Long> connectionTaskIds) {
        super(connectionTaskIds);
        this.connectionTaskService = connectionTaskService;
        this.validateConnectionTaskIds();
    }

    private void validateConnectionTaskIds() {
        this.connectionTasks = new ArrayList<>(this.getBusinessObjectIds().size());
        for (Long connectionTaskId : this.getBusinessObjectIds()) {
            this.connectionTasks.add(this.findConnectionTask(connectionTaskId));
        }
    }

    private ConnectionTask findConnectionTask(long connectionTaskId) {
        Optional<ConnectionTask> connectionTask = this.connectionTaskService.findConnectionTask(connectionTaskId);
        if (connectionTask.isPresent()) {
            return connectionTask.get();
        }
        throw new NotFoundException("ConnectionTask with id " + connectionTaskId + " not found");
    }

    @Override
    public void applyTo(EventPublisher eventPublisher) {
        eventPublisher.narrowInterestToConnectionTasks(null, this.connectionTasks);
    }

}