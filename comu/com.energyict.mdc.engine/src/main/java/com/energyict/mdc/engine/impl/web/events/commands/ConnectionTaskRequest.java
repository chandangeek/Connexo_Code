package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.common.NotFoundException;
import com.energyict.comserver.collections.Collections;
import com.energyict.mdc.engine.impl.events.EventPublisher;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Provides an implementation for the {@link Request} interface
 * that represents a request to register interest
 * in events that relate to a single {@link ConnectionTask}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-05 (09:59)
 */
public class ConnectionTaskRequest extends IdBusinessObjectRequest {

    private List<ConnectionTask> connectionTasks;

    public ConnectionTaskRequest (long connectionTaskId) {
        this(Collections.toSet(connectionTaskId));
    }

    public ConnectionTaskRequest (Set<Long> connectionTaskIds) {
        super(connectionTaskIds);
        this.validateConnectionTaskIds();
    }

    private void validateConnectionTaskIds () {
        this.connectionTasks = new ArrayList<>(this.getBusinessObjectIds().size());
        for (Long connectionTaskId : this.getBusinessObjectIds()) {
            this.connectionTasks.add(this.findConnectionTask(connectionTaskId));
        }
    }

    private ConnectionTask findConnectionTask (long connectionTaskId) {
        ConnectionTask connectionTask = ManagerFactory.getCurrent().getConnectionTaskFactory().find(connectionTaskId);
        if (connectionTask == null) {
            throw new NotFoundException("ConnectionTask with id " + connectionTaskId + " not found");
        }
        else {
            return connectionTask;
        }
    }

    @Override
    public void applyTo (EventPublisher eventPublisher) {
        eventPublisher.narrowInterestToConnectionTasks(null, this.connectionTasks);
    }

}