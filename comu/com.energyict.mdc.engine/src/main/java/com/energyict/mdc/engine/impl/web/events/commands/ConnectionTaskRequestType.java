package com.energyict.mdc.engine.impl.web.events.commands;

import java.util.Set;

/**
 * Provides an implementation for the {@link RequestType} interface
 * for {@link com.energyict.mdc.tasks.ConnectionTask}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-15 (16:59)
 */
public class ConnectionTaskRequestType extends IdBusinessObjectRequestType {

    @Override
    protected String getBusinessObjectTypeName () {
        return "connectiontask";
    }

    @Override
    protected Request newRequestForAll () {
        return new AllConnectionTasksRequest();
    }

    @Override
    protected Request newRequestFor (Set<Integer> ids) {
        return new ConnectionTaskRequest(ids);
    }

}