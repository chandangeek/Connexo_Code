/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.groups;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.spi.QueryProvider;
import com.elster.jupiter.search.SearchablePropertyValue;

import aQute.bnd.annotation.ProviderType;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@ProviderType
public interface MeteringGroupsService {

    String COMPONENTNAME = "MTG";

    <T extends Group<?>> Optional<T> findGroupByName(String name, Class<T> api);

    GroupBuilder.QueryGroupBuilder<UsagePoint, ? extends QueryUsagePointGroup> createQueryUsagePointGroup(SearchablePropertyValue... conditions);

    GroupBuilder.EnumeratedGroupBuilder<UsagePoint, ? extends EnumeratedUsagePointGroup> createEnumeratedUsagePointGroup(UsagePoint... usagePoints);

    Optional<QueryUsagePointGroup> findQueryUsagePointGroup(long id);

    Optional<EnumeratedUsagePointGroup> findEnumeratedUsagePointGroup(long id);

    Optional<UsagePointGroup> findUsagePointGroup(String mRID);

    Optional<UsagePointGroup> findUsagePointGroup(long id);

    default Optional<UsagePointGroup> findUsagePointGroupByName(String name) {
        return findGroupByName(name, UsagePointGroup.class);
    }

    GroupBuilder.QueryGroupBuilder<EndDevice, ? extends QueryEndDeviceGroup> createQueryEndDeviceGroup(SearchablePropertyValue... conditions);

    GroupBuilder.EnumeratedGroupBuilder<EndDevice, ? extends EnumeratedEndDeviceGroup> createEnumeratedEndDeviceGroup(EndDevice... endDevices);

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

    List<EnumeratedUsagePointGroup> findEnumeratedUsagePointGroupsContaining(UsagePoint usagePoint);

    Optional<EndDeviceGroup> findEndDeviceGroup(String mRID);

    Optional<EndDeviceGroup> findEndDeviceGroup(long id);

    Optional<EndDeviceGroup> findAndLockEndDeviceGroupByIdAndVersion(long id, long version);

    Optional<UsagePointGroup> findAndLockUsagePointGroupByIdAndVersion(long id, long version);

    default Optional<EndDeviceGroup> findEndDeviceGroupByName(String name) {
        return findGroupByName(name, EndDeviceGroup.class);
    }

    void addQueryProvider(QueryProvider<?> queryProvider);

    Query<EndDeviceGroup> getEndDeviceGroupQuery();

    Query<EndDeviceGroup> getQueryEndDeviceGroupQuery();

    Query<UsagePointGroup> getQueryUsagePointGroupQuery();

    List<EndDeviceGroup> findEndDeviceGroups();

    List<UsagePointGroup> findUsagePointGroups();

    Optional<QueryProvider<?>> pollQueryProvider(String name, Duration duration) throws InterruptedException;

    Query<UsagePointGroup> getUsagePointGroupQuery();

    //Finder<EndDeviceGroup> findAllEndDeviceGroups();

}
