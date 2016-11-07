package com.elster.jupiter.usagepoint.lifecycle.rest.impl;

import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCycleInfo;

import javax.inject.Inject;

public class ResourceHelper {
    private final ExceptionFactory exceptionFactory;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService;

    @Inject
    public ResourceHelper(ExceptionFactory exceptionFactory,
                          ConcurrentModificationExceptionFactory conflictFactory,
                          UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService) {
        this.exceptionFactory = exceptionFactory;
        this.conflictFactory = conflictFactory;
        this.usagePointLifeCycleConfigurationService = usagePointLifeCycleConfigurationService;
    }

    public UsagePointLifeCycle getLifeCycleByIdOrThrowException(long id) {
        return this.usagePointLifeCycleConfigurationService.findUsagePointLifeCycle(id)
                .orElseThrow(() -> this.exceptionFactory.newException(MessageSeeds.NO_SUCH_LIFE_CYCLE, id));
    }

    private Long getCurrentLifeCycleVersion(long id) {
        return this.usagePointLifeCycleConfigurationService.findUsagePointLifeCycle(id).map(UsagePointLifeCycle::getVersion).orElse(null);
    }

    public UsagePointLifeCycle lockLifeCycle(UsagePointLifeCycleInfo lifeCycleInfo) {
        return this.usagePointLifeCycleConfigurationService.findAndLockUsagePointLifeCycleByIdAndVersion(lifeCycleInfo.id, lifeCycleInfo.version)
                .orElseThrow(this.conflictFactory.contextDependentConflictOn(lifeCycleInfo.name)
                        .withActualVersion(() -> getCurrentLifeCycleVersion(lifeCycleInfo.id))
                        .supplier());
    }
}
