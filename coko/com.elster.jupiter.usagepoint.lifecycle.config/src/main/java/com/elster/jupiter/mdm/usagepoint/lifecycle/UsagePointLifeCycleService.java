package com.elster.jupiter.mdm.usagepoint.lifecycle;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

@ProviderType
public interface UsagePointLifeCycleService {
    String COMPONENT_NAME = "UPL";

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
}
