package com.elster.jupiter.metering.groups;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.common.services.Finder;
import com.google.common.base.Optional;

import java.util.List;

public interface MeteringGroupsService {
    String COMPONENTNAME = "MTG";

    QueryUsagePointGroup createQueryUsagePointGroup(Condition condition);

    EnumeratedUsagePointGroup createEnumeratedUsagePointGroup(String name);

    Optional<QueryUsagePointGroup> findQueryUsagePointGroup(long id);

    Optional<EnumeratedUsagePointGroup> findEnumeratedUsagePointGroup(long id);

    Optional<UsagePointGroup> findUsagePointGroup(String mRID);


    QueryEndDeviceGroup createQueryEndDeviceGroup(Condition condition);

    EnumeratedEndDeviceGroup createEnumeratedEndDeviceGroup(String name);

    Optional<QueryEndDeviceGroup> findQueryEndDeviceGroup(long id);

    Optional<EnumeratedEndDeviceGroup> findEnumeratedEndDeviceGroup(long id);

    Optional<EndDeviceGroup> findEndDeviceGroup(String mRID);

    EndDeviceQueryProvider getEndDeviceQueryProvider(String name);

    void addEndDeviceQueryProvider(EndDeviceQueryProvider endDeviceQueryProvider);

    List<EndDeviceGroup> findEndDeviceGroups();

    Finder<EndDeviceGroup> findAllEndDeviceGroups();

}
