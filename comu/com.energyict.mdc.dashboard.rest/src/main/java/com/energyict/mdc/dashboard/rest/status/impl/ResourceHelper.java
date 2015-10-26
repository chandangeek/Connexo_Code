package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Optional;

public class ResourceHelper {
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final CommunicationTaskService communicationTaskService;
    private final ConnectionTaskService connectionTaskService;

    @Inject
    public ResourceHelper(ConcurrentModificationExceptionFactory conflictFactory, CommunicationTaskService communicationTaskService, ConnectionTaskService connectionTaskService) {
        this.conflictFactory = conflictFactory;
        this.communicationTaskService = communicationTaskService;
        this.connectionTaskService = connectionTaskService;
    }

    public ComTaskExecution findComTaskExecutionOrThrowException(long id) {
        return communicationTaskService.findComTaskExecution(id).orElseThrow(() -> new WebApplicationException("No ComTaskExecution with id " + id,
                Response.status(Response.Status.NOT_FOUND).entity("No ComTaskExecution with id " + id).build()));
    }

    public Long getCurrentComTaskExecutionVersion(long id){
        return communicationTaskService.findComTaskExecution(id)
                .filter(candidate -> !candidate.isObsolete())
                .map(ComTaskExecution::getVersion)
                .orElse(null);
    }

    public Optional<ComTaskExecution> getLockedComTaskExecution(long id, long version){
        return communicationTaskService.findAndLockComTaskExecutionByIdAndVersion(id, version)
                .filter(candidate -> !candidate.isObsolete());
    }

    public ComTaskExecution lockComTaskExecutionOrThrowException(ComTaskExecutionInfo info) {
        return getLockedComTaskExecution(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> getCurrentComTaskExecutionVersion(info.id))
                        .supplier());
    }

    public ConnectionTask<?, ?> findConnectionTaskOrThrowException(long id) {
        return connectionTaskService.findConnectionTask(id).orElseThrow(() -> new WebApplicationException("No ConnectionTask with id " + id,
                Response.status(Response.Status.NOT_FOUND).entity("No ConnectionTask with id " + id).build()));
    }

    public Long getCurrentConnectionTaskVersion(long id){
        return connectionTaskService.findConnectionTask(id)
                .filter(candidate -> !candidate.isObsolete())
                .map(ConnectionTask::getVersion)
                .orElse(null);
    }

    public Optional<ConnectionTask> getLockedConnectionTask(long id, long version){
        return connectionTaskService.findAndLockConnectionTaskByIdAndVersion(id, version)
                .filter(candidate -> !candidate.isObsolete());
    }

    public ConnectionTask lockConnectionTaskOrThrowException(ConnectionTaskInfo info) {
        return getLockedConnectionTask(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.device.name)
                        .withActualVersion(() -> getCurrentComTaskExecutionVersion(info.id))
                        .supplier());
    }
}
