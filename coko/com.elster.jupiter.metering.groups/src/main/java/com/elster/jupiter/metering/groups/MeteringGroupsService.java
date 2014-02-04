package com.elster.jupiter.metering.groups;

import com.elster.jupiter.util.conditions.Condition;
import com.google.common.base.Optional;

public interface MeteringGroupsService {
    String COMPONENTNAME = "MTG";

    QueryUsagePointGroup createQueryUsagePointGroup(Condition condition);

    EnumeratedUsagePointGroup createEnumeratedUsagePointGroup(String name);

    Optional<QueryUsagePointGroup> findQueryUsagePointGroup(long id);

    Optional<EnumeratedUsagePointGroup> findEnumeratedUsagePointGroup(long id);

    Optional<UsagePointGroup> findUsagePointGroup(String mRID);

}
