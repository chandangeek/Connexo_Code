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

    Optional<UsagePointTransition> finUsagePointTransition(long id);
}
