/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.rest.impl;

import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.StateChangeBusinessProcess;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointState;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointTransition;
import com.elster.jupiter.usagepoint.lifecycle.rest.BusinessProcessInfo;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCycleInfo;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCycleStateInfo;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCycleTransitionInfo;

import javax.inject.Inject;

public class ResourceHelper {
    private final ExceptionFactory exceptionFactory;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService;
    private final FiniteStateMachineService finiteStateMachineService;
    private final MeteringService meteringService;

    @Inject
    public ResourceHelper(ExceptionFactory exceptionFactory,
                          ConcurrentModificationExceptionFactory conflictFactory,
                          UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService,
                          FiniteStateMachineService finiteStateMachineService,
                          MeteringService meteringService) {
        this.exceptionFactory = exceptionFactory;
        this.conflictFactory = conflictFactory;
        this.usagePointLifeCycleConfigurationService = usagePointLifeCycleConfigurationService;
        this.finiteStateMachineService = finiteStateMachineService;
        this.meteringService = meteringService;
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

    public UsagePointTransition getTransitionByIdOrThrowException(long id) {
        return this.usagePointLifeCycleConfigurationService.findUsagePointTransition(id)
                .orElseThrow(() -> this.exceptionFactory.newException(MessageSeeds.NO_SUCH_LIFE_CYCLE_TRANSITION, id));
    }

    private Long getCurrentTransitionVersion(long id) {
        return this.usagePointLifeCycleConfigurationService.findUsagePointTransition(id).map(UsagePointTransition::getVersion).orElse(null);
    }

    public UsagePointTransition lockTransition(UsagePointLifeCycleTransitionInfo transitionInfo) {
        this.usagePointLifeCycleConfigurationService.findAndLockUsagePointLifeCycleByIdAndVersion(transitionInfo.parent.id, transitionInfo.parent.version)
                .orElseThrow(this.conflictFactory.contextDependentConflictOn(transitionInfo.name)
                        .withActualVersion(() -> getCurrentTransitionVersion(transitionInfo.id))
                        .withActualParent(() -> getCurrentLifeCycleVersion(transitionInfo.parent.id))
                        .supplier());
        return this.usagePointLifeCycleConfigurationService.findAndLockUsagePointTransitionByIdAndVersion(transitionInfo.id, transitionInfo.version)
                .orElseThrow(this.conflictFactory.contextDependentConflictOn(transitionInfo.name)
                        .withActualVersion(() -> getCurrentTransitionVersion(transitionInfo.id))
                        .withActualParent(() -> getCurrentLifeCycleVersion(transitionInfo.parent.id))
                        .supplier());
    }

    public UsagePoint getUsagePointOrThrowException(String name) {
        return this.meteringService.findUsagePointByName(name)
                .orElseThrow(() -> this.exceptionFactory.newException(MessageSeeds.NO_SUCH_USAGE_POINT, name));
    }

    private Long getUsagePointVersion(String name) {
        return this.meteringService.findUsagePointByName(name).map(UsagePoint::getVersion).orElse(null);
    }

    public UsagePoint lockUsagePoint(UsagePointStateChangeRequestInfo.UsagePointInfo info) {
        return this.meteringService.findAndLockUsagePointByNameAndVersion(info.name, info.version)
                .orElseThrow(this.conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> getUsagePointVersion(info.name))
                        .supplier());
    }
}
