/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.config;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.fsm.StageSet;
import com.elster.jupiter.fsm.State;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@ProviderType
public interface UsagePointLifeCycleConfigurationService {
    String COMPONENT_NAME = "UPL";
    String USAGE_POINT_STAGE_SET_NAME = "USAGE_POINT_STAGE_SET";

    Finder<UsagePointLifeCycle> getUsagePointLifeCycles();

    Optional<UsagePointLifeCycle> findUsagePointLifeCycle(long id);

    Optional<UsagePointLifeCycle> findAndLockUsagePointLifeCycleByIdAndVersion(long id, long version);

    Optional<UsagePointLifeCycle> findUsagePointLifeCycleByName(String name);

    UsagePointLifeCycle newUsagePointLifeCycle(String name);

    UsagePointLifeCycle cloneUsagePointLifeCycle(String name, UsagePointLifeCycle source);

    UsagePointLifeCycle getDefaultLifeCycle();

    StageSet getDefaultStageSet();

    Finder<State> getUsagePointStates();

    Optional<State> findUsagePointState(long id);

    Optional<State> findAndLockUsagePointStateByIdAndVersion(long id, long version);

    Optional<UsagePointTransition> findUsagePointTransition(long id);

    Optional<UsagePointTransition> findAndLockUsagePointTransitionByIdAndVersion(long id, long version);

    Optional<MicroAction> getMicroActionByKey(String microActionKey);

    Set<MicroAction> getMicroActions();

    Optional<MicroCheck> getMicroCheckByKey(String microCheckKey);

    Set<MicroCheck> getMicroChecks();

    void addMicroActionFactory(UsagePointMicroActionFactory microActionFactory);

    void removeMicroActionFactory(UsagePointMicroActionFactory microActionFactory);

    void addMicroCheckFactory(UsagePointMicroCheckFactory microCheckFactory);

    void removeMicroCheckFactory(UsagePointMicroCheckFactory microCheckFactory);

    void addUsagePointLifeCycleBuilder(UsagePointLifeCycleBuilder builder);

    void removeUsagePointLifeCycleBuilder(UsagePointLifeCycleBuilder builder);

    List<Stage> getStages();
}
