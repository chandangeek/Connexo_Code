/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl.kpi;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.groups.Group;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.interval.Temporals;
import com.energyict.mdc.device.topology.impl.MessageSeeds;
import com.energyict.mdc.device.topology.impl.PersistenceIntegrationTest;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpi;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpiService;

import java.util.Optional;

import org.junit.After;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class RegisteredDevicesKpiServiceImplTest extends PersistenceIntegrationTest {

    @Override
    public void initializeMocks() {
        //NoMocksNeeded
    }

    @After
    public void cleanUp() {
        getRegisteredDevicesKpiService().findAllRegisteredDevicesKpis()
                .forEach(RegisteredDevicesKpi::delete);
        getMeteringGroupsService().findEndDeviceGroups()
                .forEach(Group::delete);
    }

    @Test
    @Transactional
    public void testPersistence() {
        //just took the available domains and query provider (doesn't need to be correct)
        QueryEndDeviceGroup queryEndDeviceGroup = getDeviceGroup();
        RegisteredDevicesKpi kpi = getRegisteredDevicesKpiService().newRegisteredDevicesKpi(queryEndDeviceGroup)
                .frequency(Temporals.toTemporalAmount(TimeDuration.hours(4)))
                .target(95)
                .save();
        Optional<RegisteredDevicesKpi> reloadedKpi = getRegisteredDevicesKpiService().findRegisteredDevicesKpi(kpi.getId());
        assertThat(reloadedKpi).isPresent();
        assertThat(reloadedKpi.get().getDeviceGroup().getId()).isEqualTo(queryEndDeviceGroup.getId());
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FREQUENCY_MUST_BE_VALID + "}", property = "frequency", strict = false)
    public void testWrongFrequency() {
        //just took the available domains and query provider (doesn't need to be correct)
        QueryEndDeviceGroup queryEndDeviceGroup = getDeviceGroup();
        RegisteredDevicesKpi kpi = getRegisteredDevicesKpiService().newRegisteredDevicesKpi(queryEndDeviceGroup)
                .frequency(Temporals.toTemporalAmount(TimeDuration.hours(5)))
                .target(95)
                .save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.TARGET_MUST_BE_VALID + "}", property = "target", strict = false)
    public void testTargetTooHigh() {
        //just took the available domains and query provider (doesn't need to be correct)
        QueryEndDeviceGroup queryEndDeviceGroup = getDeviceGroup();
        RegisteredDevicesKpi kpi = getRegisteredDevicesKpiService().newRegisteredDevicesKpi(queryEndDeviceGroup)
                .frequency(Temporals.toTemporalAmount(TimeDuration.hours(4)))
                .target(101)
                .save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.TARGET_MUST_BE_VALID + "}", property = "target", strict = false)
    public void testTargetTooLow() {
        //just took the available domains and query provider (doesn't need to be correct)
        QueryEndDeviceGroup queryEndDeviceGroup = getDeviceGroup();
        RegisteredDevicesKpi kpi = getRegisteredDevicesKpiService().newRegisteredDevicesKpi(queryEndDeviceGroup)
                .frequency(Temporals.toTemporalAmount(TimeDuration.hours(4)))
                .target(-1)
                .save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.DEVICE_GROUP_MUST_BE_UNIQUE + "}", property = "deviceGroup", strict = false)
    public void testCantCreateTwoKpisForSameGroup() {
        //just took the available domains and query provider (doesn't need to be correct)
        QueryEndDeviceGroup queryEndDeviceGroup = getDeviceGroup();
        getRegisteredDevicesKpiService().newRegisteredDevicesKpi(queryEndDeviceGroup)
                .frequency(Temporals.toTemporalAmount(TimeDuration.hours(4)))
                .target(5)
                .save();
        getRegisteredDevicesKpiService().newRegisteredDevicesKpi(queryEndDeviceGroup)
                .frequency(Temporals.toTemporalAmount(TimeDuration.hours(4)))
                .target(5)
                .save();
    }

    private QueryEndDeviceGroup getDeviceGroup() {
        return getMeteringGroupsService().createQueryEndDeviceGroup()
                    .setAliasName("group")
                    .setDescription("description")
                    .setSearchDomain(getSearchService().getDomains().get(0))
                    .setLabel("label")
                    .setName("name")
                    .setQueryProviderName("com.energyict.mdc.device.data.impl.DeviceEndDeviceQueryProvider")
                    .setMRID("MDC: name")
                    .create();
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
