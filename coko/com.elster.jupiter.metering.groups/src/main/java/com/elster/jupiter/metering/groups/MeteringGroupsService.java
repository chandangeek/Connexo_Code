package com.elster.jupiter.metering.groups;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.util.conditions.Condition;

import java.time.Duration;
import java.util.Optional;

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

    /**
     * Find all {@link EnumeratedEndDeviceGroup}s that contain the specified {@link EndDevice}.
     *
     * @param endDevice The EndDevice
     * @return The List of EnumeratedEndDeviceGroup
     * @since 1.1
     */
    List<EnumeratedEndDeviceGroup> findEnumeratedEndDeviceGroupsContaining(EndDevice endDevice);

    Optional<EndDeviceGroup> findEndDeviceGroup(String mRID);

    Optional<EndDeviceGroup> findEndDeviceGroup(long id);

    Optional<EndDeviceGroup> findEndDeviceGroupByName(String name);

    void addEndDeviceQueryProvider(EndDeviceQueryProvider endDeviceQueryProvider);

    Query<EndDeviceGroup> getEndDeviceGroupQuery();
    Query<EndDeviceGroup> getQueryEndDeviceGroupQuery();

    List<EndDeviceGroup> findEndDeviceGroups();

    Optional<EndDeviceQueryProvider> pollEndDeviceQueryProvider(String name, Duration duration) throws InterruptedException;

    //Finder<EndDeviceGroup> findAllEndDeviceGroups();

}
