package com.elster.jupiter.mdm.usagepoint.lifecycle;

import java.util.Optional;

public interface UsagePointLifeCycleService {
    String COMPONENT_NAME = "UPL";

    Optional<UsagePointLifeCycle> findUsagePointLifeCycleByName(String name);

    UsagePointLifeCycleBuilder newUsagePointLifeCycle(String name);
}
