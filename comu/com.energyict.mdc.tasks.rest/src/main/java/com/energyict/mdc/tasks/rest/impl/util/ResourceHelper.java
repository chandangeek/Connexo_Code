/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.rest.impl.util;

import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.tasks.rest.impl.ComTaskInfo;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Optional;

@SuppressWarnings("PackageAccessibility")
public class ResourceHelper {

    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final TaskService taskService;

    @Inject
    public ResourceHelper(ConcurrentModificationExceptionFactory conflictFactory, TaskService taskService) {
        this.conflictFactory = conflictFactory;
        this.taskService = taskService;
    }

    public ComTask findComTaskOrThrowException(long id) {
        return taskService.findComTask(id).orElseThrow(() -> new WebApplicationException("No ComTask with id " + id,
                Response.status(Response.Status.NOT_FOUND).entity("No ComTask with id " + id).build()));
    }

    public Long getCurrentComTaskVersion(long comTaskId){
        return taskService.findComTask(comTaskId)
                .map(ComTask::getVersion)
                .orElse(null);
    }

    public Optional<ComTask> getLockedComTask(long comTaskId, long version){
        return taskService.findAndLockComTaskByIdAndVersion(comTaskId, version);
    }

    public ComTask lockComTaskOrThrowException(ComTaskInfo info) {
        return getLockedComTask(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> getCurrentComTaskVersion(info.id))
                        .supplier());
    }


}
