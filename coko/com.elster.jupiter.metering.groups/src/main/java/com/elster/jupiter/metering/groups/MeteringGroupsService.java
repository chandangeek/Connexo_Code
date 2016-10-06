package com.elster.jupiter.metering.groups;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.groups.spi.EndDeviceQueryProvider;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.util.conditions.Condition;

import aQute.bnd.annotation.ProviderType;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@ProviderType
public interface MeteringGroupsService {

    String COMPONENTNAME = "MTG";

    UsagePointGroupBuilder.QueryUsagePointGroupBuilder createQueryUsagePointGroup(Condition condition);

    UsagePointGroupBuilder.EnumeratedUsagePointGroupBuilder createEnumeratedUsagePointGroup();

    Optional<QueryUsagePointGroup> findQueryUsagePointGroup(long id);

    Optional<EnumeratedUsagePointGroup> findEnumeratedUsagePointGroup(long id);

    Optional<UsagePointGroup> findUsagePointGroup(String mRID);

    Optional<UsagePointGroup> findUsagePointGroup(long id);

    Optional<UsagePointGroup> findUsagePointGroupByName(String name);

    EndDeviceGroupBuilder.QueryEndDeviceGroupBuilder createQueryEndDeviceGroup(SearchablePropertyValue... conditions);

    EndDeviceGroupBuilder.EnumeratedEndDeviceGroupBuilder createEnumeratedEndDeviceGroup(EndDevice... endDevices);

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

    Optional<EndDeviceGroup> findAndLockEndDeviceGroupByIdAndVersion(long id, long version);

    Optional<EndDeviceGroup> findEndDeviceGroupByName(String name);

    void addEndDeviceQueryProvider(EndDeviceQueryProvider endDeviceQueryProvider);

    Query<EndDeviceGroup> getEndDeviceGroupQuery();

    Query<EndDeviceGroup> getQueryEndDeviceGroupQuery();

    List<EndDeviceGroup> findEndDeviceGroups();

    List<UsagePointGroup> findUsagePointGroups();

    Optional<EndDeviceQueryProvider> pollEndDeviceQueryProvider(String name, Duration duration) throws InterruptedException;

    Query<UsagePointGroup> getUsagePointGroupQuery();

    //Finder<EndDeviceGroup> findAllEndDeviceGroups();

}
