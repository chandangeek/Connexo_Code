/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.rest.impl.comserver;

import com.elster.jupiter.rest.util.ConcurrentModificationException;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionBuilder;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class ResourceHelper {
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final EngineConfigurationService engineConfigurationService;

    @Inject
    public ResourceHelper(ConcurrentModificationExceptionFactory conflictFactory, EngineConfigurationService engineConfigurationService) {
        this.conflictFactory = conflictFactory;
        this.engineConfigurationService = engineConfigurationService;
    }

    public Supplier<ConcurrentModificationException> getConcurrentExSupplier(String name, Supplier<Long> actualVersionSupplier){
        return conflictFactory.contextDependentConflictOn(name)
                .withActualVersion(actualVersionSupplier)
                .supplier();
    }

    public ComServer findComServerOrThrowException(long id) {
        return engineConfigurationService.findComServer(id).orElseThrow(() -> new WebApplicationException("No ComServer with id " + id,
                Response.status(Response.Status.NOT_FOUND).entity("No ComServer with id " + id).build()));
    }

    public Long getCurrentComServerVersion(long id){
        return engineConfigurationService.findComServer(id)
                .filter(candidate -> !candidate.isObsolete())
                .map(ComServer::getVersion)
                .orElse(null);
    }

    public Optional<ComServer> getLockedComServer(long id, long version){
        return engineConfigurationService.findAndLockComServerByIdAndVersion(id, version)
                .filter(candidate -> !candidate.isObsolete());
    }

    public ComServer lockComServerOrThrowException(ComServerInfo<?, ?> info) {
        return getLockedComServer(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> getCurrentComServerVersion(info.id))
                        .supplier());
    }

    public ComPort findComPortOrThrowException(long id) {
        return engineConfigurationService.findComPort(id)
                .filter(candidate -> !candidate.isObsolete())
                .orElseThrow(() -> new WebApplicationException("No ComPort with id " + id + " found.",
                        Response.status(Response.Status.NOT_FOUND).entity("No ComPort with id " + id + " found.").build()));
    }

    public ComPort findComPortOrThrowException(long id, ComServer comServer) {
        Objects.requireNonNull(comServer);
        return comServer.getComPorts()
                .stream()
                .filter(candidate -> candidate.getId() == id && !candidate.isObsolete())
                .findFirst()
                .orElseThrow(() -> new WebApplicationException("No ComPort with id " + id + " found for ComServer " + comServer.getId(),
                        Response.status(Response.Status.NOT_FOUND).entity("No ComPort with id " + id + " found for ComServer " + comServer.getId()).build()));
    }

    public ComPort findComPortOrThrowException(long id, ComPortPool comPortPool) {
        Objects.requireNonNull(comPortPool);
        return comPortPool.getComPorts()
                .stream()
                .filter(candidate -> candidate.getId() == id && !candidate.isObsolete())
                .findFirst()
                .orElseThrow(() -> new WebApplicationException("No ComPort with id " + id + " found for ComPortPool " + comPortPool.getId(),
                        Response.status(Response.Status.NOT_FOUND).entity("No ComPort with id " + id + " found for ComPortPool " + comPortPool.getId()).build()));
    }

    public Long getCurrentComPortVersion(long id){
        return engineConfigurationService.findComPort(id)
                .filter(candidate -> !candidate.isObsolete())
                .map(ComPort::getVersion)
                .orElse(null);
    }

    public Optional<? extends ComPort> getLockedComPort(long id, long version){
        return engineConfigurationService.findAndLockComPortByIdAndVersion(id, version)
                .filter(candidate -> !candidate.isObsolete());
    }

    public ComPort lockComPortOrThrowException(ComPortInfo<?, ?> info) {
        Optional<ComServer> comServer = getLockedComServer(info.parent.id, info.parent.version);
        ConcurrentModificationExceptionBuilder modificationExceptionBuilder = conflictFactory.contextDependentConflictOn(info.name);
        if (comServer.isPresent()) {
            return getLockedComPort(info.id, info.version)
                    .orElseThrow(modificationExceptionBuilder
                            .withActualParent(() -> getCurrentComServerVersion(info.parent.id), info.parent.id)
                            .withActualVersion(() -> getCurrentComPortVersion(info.id))
                            .supplier());
        }
        throw modificationExceptionBuilder
                .withActualParent(() -> getCurrentComServerVersion(info.parent.id), info.parent.id)
                .withActualVersion(() -> getCurrentComPortVersion(info.id))
                .build();

    }

    public ComPortPool findComPortPoolOrThrowException(long id) {
        return engineConfigurationService.findComPortPool(id)
                .filter(candidate -> !candidate.isObsolete())
                .orElseThrow(() -> new WebApplicationException("No ComPortPool with id " + id + " found.",
                        Response.status(Response.Status.NOT_FOUND).entity("No ComPortPool with id " + id + " found.").build()));
    }

    public Long getCurrentComPortPoolVersion(long id){
        return engineConfigurationService.findComPortPool(id)
                .filter(candidate -> !candidate.isObsolete())
                .map(ComPortPool::getVersion)
                .orElse(null);
    }

    public Optional<? extends ComPortPool> getLockedComPortPool(long id, long version){
        return engineConfigurationService.findAndLockComPortPoolByIdAndVersion(id, version)
                .filter(candidate -> !candidate.isObsolete());
    }

    public ComPortPool lockComPortPoolOrThrowException(ComPortPoolInfo info) {
        return getLockedComPortPool(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> getCurrentComPortPoolVersion(info.id))
                        .supplier());
    }
}