/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.device.data.tasks.ConnectionTaskService;

import java.util.Set;

/**
 * Provides an implementation for the {@link RequestType} interface
 * for {@link com.energyict.mdc.device.data.tasks.ConnectionTask}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-15 (16:59)
 */
class ConnectionTaskRequestType extends IdBusinessObjectRequestType {

    private final ConnectionTaskService connectionTaskService;

    ConnectionTaskRequestType(ConnectionTaskService connectionTaskService) {
        this.connectionTaskService = connectionTaskService;
    }

    @Override
    protected String getBusinessObjectTypeName() {
        return "connectiontask";
    }

    @Override
    protected Request newRequestForAll() {
        return new AllConnectionTasksRequest();
    }

    @Override
    protected Request newRequestFor(Set<Long> ids) {
        return new ConnectionTaskRequest(this.connectionTaskService, ids);
    }

}