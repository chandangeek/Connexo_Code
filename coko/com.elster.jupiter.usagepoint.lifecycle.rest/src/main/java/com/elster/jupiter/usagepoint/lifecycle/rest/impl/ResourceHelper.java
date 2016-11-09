package com.elster.jupiter.usagepoint.lifecycle.rest.impl;

import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.StateChangeBusinessProcess;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;
import com.elster.jupiter.usagepoint.lifecycle.rest.BusinessProcessInfo;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCycleInfo;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCycleStateInfo;

import javax.inject.Inject;

public class ResourceHelper {
    private final ExceptionFactory exceptionFactory;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService;
    private final FiniteStateMachineService finiteStateMachineService;

    @Inject
    public ResourceHelper(ExceptionFactory exceptionFactory,
                          ConcurrentModificationExceptionFactory conflictFactory,
                          UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService,
                          FiniteStateMachineService finiteStateMachineService) {
        this.exceptionFactory = exceptionFactory;
        this.conflictFactory = conflictFactory;
        this.usagePointLifeCycleConfigurationService = usagePointLifeCycleConfigurationService;
        this.finiteStateMachineService = finiteStateMachineService;
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

    public UsagePointLifeCycle lockLifeCycle(VersionInfo<Long> lifeCycleInfo) {
        return this.usagePointLifeCycleConfigurationService.findAndLockUsagePointLifeCycleByIdAndVersion(lifeCycleInfo.id, lifeCycleInfo.version)
                .orElseThrow(this.conflictFactory.contextDependentConflictOn(String.valueOf(lifeCycleInfo.id))
                        .withActualVersion(() -> getCurrentLifeCycleVersion(lifeCycleInfo.id))
                        .supplier());
    }

    public UsagePointState getStateByIdOrThrowException(long id) {
        return this.usagePointLifeCycleConfigurationService.findUsagePointState(id)
                .orElseThrow(() -> this.exceptionFactory.newException(MessageSeeds.NO_SUCH_LIFE_CYCLE_STATE, id));
    }

    public StateChangeBusinessProcess getBpmProcessOrThrowException(BusinessProcessInfo info) {
        return getBpmProcessOrThrowException(info.id);
    }

    public StateChangeBusinessProcess getBpmProcessOrThrowException(long id) {
        return this.finiteStateMachineService.findStateChangeBusinessProcessById(id)
                .orElseThrow(() -> this.exceptionFactory.newException(MessageSeeds.NO_SUCH_BUSINESS_PROCESS, id));
    }

    private Long getCurrentStateVersion(long id) {
        return this.usagePointLifeCycleConfigurationService.findUsagePointState(id).map(UsagePointState::getVersion).orElse(null);
    }

    public UsagePointState lockState(UsagePointLifeCycleStateInfo stateInfo) {
        this.usagePointLifeCycleConfigurationService.findAndLockUsagePointLifeCycleByIdAndVersion(stateInfo.parent.id, stateInfo.parent.version)
                .orElseThrow(this.conflictFactory.contextDependentConflictOn(stateInfo.name)
                        .withActualVersion(() -> getCurrentStateVersion(stateInfo.id))
                        .withActualParent(() -> getCurrentLifeCycleVersion(stateInfo.parent.id))
                        .supplier());
        return this.usagePointLifeCycleConfigurationService.findAndLockUsagePointStateByIdAndVersion(stateInfo.id, stateInfo.version)
                .orElseThrow(this.conflictFactory.contextDependentConflictOn(stateInfo.name)
                        .withActualVersion(() -> getCurrentStateVersion(stateInfo.id))
                        .withActualParent(() -> getCurrentLifeCycleVersion(stateInfo.parent.id))
                        .supplier());
    }
}
