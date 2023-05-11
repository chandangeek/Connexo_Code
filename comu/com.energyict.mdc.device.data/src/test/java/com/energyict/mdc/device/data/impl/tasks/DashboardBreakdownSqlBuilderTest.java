/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyOperator;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.energyict.mdc.device.data.impl.DeviceEndDeviceQueryProvider;
import com.energyict.mdc.device.data.impl.PersistenceIntegrationTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DashboardBreakdownSqlBuilderTest extends PersistenceIntegrationTest {

    @Test
    @Transactional
    public void testDynamicGroupDataQuery() {
        QueryEndDeviceGroup queryEndDeviceGroup1 = inMemoryPersistence.getMeteringGroupsService().createQueryEndDeviceGroup()
                .setName("QueryEndDeviceGroup")
                .setMRID("MRID")
                .setQueryProviderName(DeviceEndDeviceQueryProvider.DEVICE_END_DEVICE_QUERY_PROVIDER)
                .setSearchDomain(inMemoryPersistence.getDeviceSearchDomain())
                .withConditions(buildSearchablePropertyCondition("name", SearchablePropertyOperator.EQUAL, "devicenam*"))
                .create();

        List<QueryEndDeviceGroup> queryEndDeviceGroups = new ArrayList<>();
        queryEndDeviceGroups.add(queryEndDeviceGroup1);

        DashboardBreakdownSqlBuilder sqlBuilder = new DashboardBreakdownSqlBuilder();

        assertEquals("CREATE GLOBAL TEMPORARY TABLE DYNAMIC_GROUP_DATA on commit preserve rows AS ( " +
                        "select 1 as group_id, id as device_id from (" +
                        "select D1.ID from DDC_DEVICE D1 where (D1.ID) IN (" +
                        "select id from (" +
                        "select dev.ID, dev.VERSIONCOUNT, dev.CREATETIME, dev.MODTIME, dev.USERNAME, dev.NAME, dev.SERIALNUMBER, dev.TIMEZONE, dev.MRID, dev.CERTIF_YEAR, " +
                        "dev.DEVICETYPE, dev.DEVICECONFIGID, dev.METERID, dev.BATCH_ID, dev.ESTIMATION_ACTIVE, dev.PASSIVE_CAL, dev.PLANNED_PASSIVE_CAL " +
                        "from  DDC_DEVICE dev where (nvl(dev.name, '') LIKE 'devicenam%' ESCAPE '\\')))))",
                sqlBuilder.getDynamicGroupDataQuery(queryEndDeviceGroups));
    }

    private SearchablePropertyValue buildSearchablePropertyCondition(String property, SearchablePropertyOperator operator, String... values) {
        SearchDomain deviceSearchDomain = inMemoryPersistence.getDeviceSearchDomain();
        Optional<SearchableProperty> searchableProperty = deviceSearchDomain.getProperties().stream().filter(p -> property.equals(p.getName())).findFirst();
        if (searchableProperty.isPresent()) {
            return new SearchablePropertyValue(searchableProperty.get(), new SearchablePropertyValue.ValueBean(searchableProperty.get().getName(), operator, values));
        }
        throw new IllegalArgumentException("Searchable property with name '" + property + "' is not found");
    }
}
