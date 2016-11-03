package com.elster.jupiter.usagepoint.lifecycle.config;

import com.elster.jupiter.domain.util.Finder;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

@ProviderType
public interface UsagePointLifeCycleConfigurationService {
    String COMPONENT_NAME = "UPL";

    Finder<UsagePointLifeCycle> getUsagePointLifeCycles();

    Optional<UsagePointLifeCycle> findUsagePointLifeCycle(long id);

    Optional<UsagePointLifeCycle> findAndLockUsagePointLifeCycleByIdAndVersion(long id, long version);

    Optional<UsagePointLifeCycle> findUsagePointLifeCycleByName(String name);

    UsagePointLifeCycle newUsagePointLifeCycle(String name);

    UsagePointLifeCycle cloneUsagePointLifeCycle(String name, UsagePointLifeCycle source);

    Optional<UsagePointState> findUsagePointState(long id);

    Optional<UsagePointState> findAndLockUsagePointStateByIdAndVersion(long id, long version);

    Optional<UsagePointTransition> findUsagePointTransition(long id);

    Optional<UsagePointTransition> findAndLockUsagePointTransitionByIdAndVersion(long id, long version);

    Optional<MicroAction> getMicroActionByKey(String microActionKey);

    Optional<MicroCheck> getMicroCheckByKey(String microCheckKey);

    void addMicroActionFactory(UsagePointMicroActionFactory microActionFactory);

    void removeMicroActionFactory(UsagePointMicroActionFactory microActionFactory);

    void addMicroCheckFactory(UsagePointMicroCheckFactory microCheckFactory);

    void removeMicroCheckFactory(UsagePointMicroCheckFactory microCheckFactory);

    void addUsagePointLifeCycleBuilder(UsagePointLifeCycleBuilder builder);

    void removeUsagePointLifeCycleBuilder(UsagePointLifeCycleBuilder builder);
}
