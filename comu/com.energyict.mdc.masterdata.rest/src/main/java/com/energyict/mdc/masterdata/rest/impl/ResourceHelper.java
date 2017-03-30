/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.rest.impl;

import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.masterdata.rest.LoadProfileTypeInfo;
import com.energyict.mdc.masterdata.rest.LogBookTypeInfo;
import com.energyict.mdc.masterdata.rest.RegisterTypeInfo;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Optional;

public class ResourceHelper {
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final MasterDataService masterDataService;

    @Inject
    public ResourceHelper(ConcurrentModificationExceptionFactory conflictFactory, MasterDataService masterDataService) {
        this.conflictFactory = conflictFactory;
        this.masterDataService = masterDataService;
    }

    public LogBookType findLogBookTypeOrThrowException(long id) {
        return masterDataService.findLogBookType(id).orElseThrow(() -> new WebApplicationException("No LogbookType with id " + id,
                Response.status(Response.Status.NOT_FOUND).entity("No LogbookType with id " + id).build()));
    }
    public Long getCurrentLogBookTypeVersion(long id) {
        return masterDataService.findLogBookType(id)
                .map(LogBookType::getVersion)
                .orElse(null);
    }
    public Optional<LogBookType> getLockedLogBookType(long id, long version) {
        return masterDataService.findAndLockLogBookTypeByIdAndVersion(id, version);
    }
    public LogBookType lockLogBookTypeOrThrowException(LogBookTypeInfo info) {
        return getLockedLogBookType(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> getCurrentLogBookTypeVersion(info.id))
                        .supplier());
    }


    public RegisterType findRegisterTypeOrThrowException(long id) {
        return masterDataService.findRegisterType(id).orElseThrow(() -> new WebApplicationException("No RegisterType with id " + id,
                Response.status(Response.Status.NOT_FOUND).entity("No RegisterType with id " + id).build()));
    }
    public Long getCurrentRegisterTypeVersion(long id) {
        return masterDataService.findRegisterType(id)
                .map(RegisterType::getVersion)
                .orElse(null);
    }
    public Optional<RegisterType> getLockedRegisterType(long id, long version) {
        return masterDataService.findAndLockRegisterTypeByIdAndVersion(id, version);
    }
    public RegisterType lockRegisterTypeOrThrowException(RegisterTypeInfo info) {
        return getLockedRegisterType(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.readingType.fullAliasName)
                        .withActualVersion(() -> getCurrentRegisterTypeVersion(info.id))
                        .supplier());
    }



    public LoadProfileType findLoadProfileTypeOrThrowException(long id) {
        return masterDataService.findLoadProfileType(id).orElseThrow(() -> new WebApplicationException("No LoadProfileType with id " + id,
                Response.status(Response.Status.NOT_FOUND).entity("No LoadProfileType with id " + id).build()));
    }
    public Long getCurrentLoadProfileTypeVersion(long id) {
        return masterDataService.findLoadProfileType(id)
                .map(LoadProfileType::getVersion)
                .orElse(null);
    }
    public Optional<LoadProfileType> getLockedLoadProfileType(long id, long version) {
        return masterDataService.findAndLockLoadProfileTypeByIdAndVersion(id, version);
    }
    public LoadProfileType lockLoadProfileTypeOrThrowException(LoadProfileTypeInfo info) {
        return getLockedLoadProfileType(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> getCurrentLoadProfileTypeVersion(info.id))
                        .supplier());
    }
}
