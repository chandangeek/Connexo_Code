/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.scheduling.rest.impl;

import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Optional;

public class ResourceHelper {

    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final SchedulingService schedulingService;

    @Inject
    public ResourceHelper(ConcurrentModificationExceptionFactory conflictFactory, SchedulingService schedulingService) {
        this.conflictFactory = conflictFactory;
        this.schedulingService = schedulingService;
    }

    public ComSchedule findComScheduleOrThrowException(long id) {
        return schedulingService.findSchedule(id)
                .orElseThrow(() -> new WebApplicationException("No such schedule", Response.Status.NOT_FOUND));
    }


    Long getCurrentComScheduleVersion(long id) {
        return schedulingService.findSchedule(id)
                .filter(candidate -> !candidate.isObsolete())
                .map(ComSchedule::getVersion)
                .orElse(null);
    }

    Optional<ComSchedule> getLockedComSchedule(long id, long version) {
        return schedulingService.findAndLockComScheduleByIdAndVersion(id, version);
    }

    ComSchedule lockComScheduleOrThrowException(ComScheduleInfo info) {
        return getLockedComSchedule(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> getCurrentComScheduleVersion(info.id))
                        .supplier());
    }

}
