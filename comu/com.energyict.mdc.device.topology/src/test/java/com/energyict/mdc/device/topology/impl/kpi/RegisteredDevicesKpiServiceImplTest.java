/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl.kpi;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.interval.Temporals;
import com.energyict.mdc.device.topology.impl.PersistenceIntegrationTest;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpi;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpiService;


import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class RegisteredDevicesKpiServiceImplTest extends PersistenceIntegrationTest {

    @Override
    public void initializeMocks() {
        //NoMocksNeeded
    }

    @Test
    @Transactional
    public void testService() {
        //just took the available domains and query provider (doesn't need to be correct)
        QueryEndDeviceGroup queryEndDeviceGroup = getMeteringGroupsService().createQueryEndDeviceGroup()
                .setAliasName("group")
                .setDescription("description")
                .setSearchDomain(getSearchService().getDomains().get(0))
                .setLabel("label")
                .setName("name")
                .setQueryProviderName("com.energyict.mdc.device.data.impl.DeviceEndDeviceQueryProvider")
                .setMRID("MDC: name")
                .create();
        RegisteredDevicesKpi kpi = getRegisteredDevicesKpiService().newRegisteredDevicesKpi(queryEndDeviceGroup)
                .frequency(Temporals.toTemporalAmount(TimeDuration.hours(2)))
                .target(95)
                .save();
        Optional<RegisteredDevicesKpi> reloadedKpi = getRegisteredDevicesKpiService().findRegisteredDevicesKpi(kpi.getId());
        assertThat(reloadedKpi).isPresent();
        assertThat(reloadedKpi.get().getDeviceGroup().getId()).isEqualTo(queryEndDeviceGroup.getId());
    }

    private MeteringGroupsService getMeteringGroupsService() {
        return inMemoryPersistence.getMeteringGroupsService();
    }

    private RegisteredDevicesKpiService getRegisteredDevicesKpiService() {
        return inMemoryPersistence.getRegisteredDevicesKpiService();
    }

    private SearchService getSearchService() {
        return inMemoryPersistence.getSearchService();
    }

}
