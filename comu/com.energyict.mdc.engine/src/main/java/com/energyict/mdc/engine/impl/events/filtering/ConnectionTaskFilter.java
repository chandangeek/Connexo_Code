package com.energyict.mdc.engine.impl.events.filtering;

import com.energyict.mdc.engine.events.ComServerEvent;
import com.energyict.mdc.engine.events.ConnectionTaskRelatedEvent;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

import java.util.List;

/**
 * Provides an implementation for the {@link EventFilterCriterion} interface
 * that will filter {@link ComServerEvent}s when they do not relate
 * to a number of {@link ConnectionTask}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-31 (14:31)
 */
public class ConnectionTaskFilter implements EventFilterCriterion {

    private List<ConnectionTask> connectionTasks;

    public ConnectionTaskFilter (List<ConnectionTask> connectionTasks) {
        super();
        this.connectionTasks = connectionTasks;
    }

    public List<ConnectionTask> getConnectionTasks () {
        return connectionTasks;
    }

    public void setConnectionTasks (List<ConnectionTask> connectionTasks) {
        this.connectionTasks = connectionTasks;
    }

    @Override
    public boolean matches (ComServerEvent event) {
        if (event.isConnectionTaskRelated()) {
            ConnectionTaskRelatedEvent connectionTaskEvent = (ConnectionTaskRelatedEvent) event;
            return !this.connectionTasks.contains(connectionTaskEvent.getConnectionTask());
        }
        else {
            return false;
        }
    }

}