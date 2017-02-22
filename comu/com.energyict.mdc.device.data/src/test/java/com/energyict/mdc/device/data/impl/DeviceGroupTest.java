/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.tests.rules.Expected;
import com.elster.jupiter.events.impl.EventServiceImpl;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyOperator;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.events.EndDeviceGroupDeletionVetoEventHandler;
import com.energyict.mdc.device.data.impl.events.VetoDeleteDeviceGroupException;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DeviceGroupTest extends PersistenceIntegrationTest {

    private static final String QUERY_EDG_NAME = "QueryEndDeviceGroup";
    private static final String ENUM_EDG_NAME = "EnumeratedEndDeviceGroup";

    private static final String DEVICE_NAME1 = "devicename1";
    private static final String DEVICE_NAME2 = "devicename2";
    private static final String DEVICE_NAME3 = "xxx";

    @Test
    @Transactional
    public void testPersistenceDynamicGroup() {
        Device device1 = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME1, Instant.now());
        device1.save();
        Device device2 = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME2, Instant.now());
        device2.save();
        Device device3 = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME3, Instant.now());
        device3.save();
        inMemoryPersistence.getMeteringGroupsService().createQueryEndDeviceGroup()
                .setName(QUERY_EDG_NAME)
                .setQueryProviderName(DeviceEndDeviceQueryProvider.DEVICE_END_DEVICE_QUERY_PROVIDER)
                .setSearchDomain(inMemoryPersistence.getDeviceSearchDomain())
                .withConditions(buildSearchablePropertyCondition("name", SearchablePropertyOperator.EQUAL, Collections.singletonList("devicename*")))
                .create();

        //Business method
        Optional<EndDeviceGroup> found = inMemoryPersistence.getMeteringGroupsService().findEndDeviceGroupByName(QUERY_EDG_NAME);

        //Asserts
        assertThat(found).isPresent();
        assertThat(found.get()).isInstanceOf(QueryEndDeviceGroup.class);
        QueryEndDeviceGroup group = (QueryEndDeviceGroup) found.get();
        List<EndDevice> members = group.getMembers(new DateTime(2014, 1, 23, 14, 54).toDate().toInstant());
        assertThat(members).hasSize(2);
        assertThat(members.stream().map(EndDevice::getAmrId).collect(Collectors.toList())).contains(String.valueOf(device1.getId()), String.valueOf(device2.getId()));
    }

    @Test
    @Transactional
    public void testPersistenceStaticGroup() {
        Device device1 = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME1, Instant.now());
        device1.save();
        Device device2 = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME2, Instant.now());
        device2.save();
        Device device3 = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME3, Instant.now());
        device3.save();
        inMemoryPersistence.getMeteringGroupsService().createEnumeratedEndDeviceGroup()
                .setName(ENUM_EDG_NAME)
                .setMRID(ENUM_EDG_NAME)
                .containing(
                        inMemoryPersistence.getMeteringService().findEndDeviceByMRID(device1.getmRID()).get(),
                        inMemoryPersistence.getMeteringService().findEndDeviceByMRID(device2.getmRID()).get())
                .at(Instant.EPOCH)
                .create();

        //Business method
        Optional<EndDeviceGroup> found = inMemoryPersistence.getMeteringGroupsService().findEndDeviceGroup(ENUM_EDG_NAME);

        //Asserts
        assertThat(found).isPresent();
        assertThat(found.get()).isInstanceOf(EnumeratedEndDeviceGroup.class);
        EnumeratedEndDeviceGroup group = (EnumeratedEndDeviceGroup) found.get();
        List<EndDevice> members = group.getMembers(new DateTime(2014, 1, 23, 14, 54).toDate().toInstant());
        assertThat(members).hasSize(2);
        assertThat(members.get(0).getAmrId()).isEqualTo(String.valueOf(device1.getId()));
        assertThat(members.get(1).getAmrId()).isEqualTo(String.valueOf(device2.getId()));
    }

    @Test
    @Transactional
    @Expected(value = VetoDeleteDeviceGroupException.class)
    public void testVetoDeletionOfDeviceGroupInKpi() throws Exception {
        DataCollectionKpiService kpiService = inMemoryPersistence.getDataCollectionKpiService();
        DataQualityKpiService dataQualityKpiService = inMemoryPersistence.getDataQualityKpiService();
        Thesaurus thesaurus = inMemoryPersistence.getThesaurusFromDeviceDataModel();
        MeteringGroupsService meteringGroupsService = inMemoryPersistence.getMeteringGroupsService();
        ((EventServiceImpl) inMemoryPersistence.getEventService()).addTopicHandler(new EndDeviceGroupDeletionVetoEventHandler(kpiService, thesaurus, dataQualityKpiService));

        QueryEndDeviceGroup queryEndDeviceGroup = meteringGroupsService.createQueryEndDeviceGroup()
                .setMRID("mine")
                .setName("mine")
                .setSearchDomain(inMemoryPersistence.getDeviceSearchDomain())
                .setQueryProviderName(DeviceEndDeviceQueryProvider.DEVICE_END_DEVICE_QUERY_PROVIDER)
                .create();

        DataCollectionKpiService.DataCollectionKpiBuilder dataCollectionKpiBuilder = kpiService.newDataCollectionKpi(queryEndDeviceGroup);
        dataCollectionKpiBuilder.frequency(Duration.ofMinutes(15)).calculateConnectionSetupKpi().expectingAsMaximum(BigDecimal.TEN);
        dataCollectionKpiBuilder.displayPeriod(TimeDuration.days(1));
        dataCollectionKpiBuilder.save();

        //Business method
        queryEndDeviceGroup.delete();

        // Asserts: see expected exception
    }

    private SearchablePropertyValue buildSearchablePropertyCondition(String property, SearchablePropertyOperator operator, List<String> values) {
        SearchDomain deviceSearchDomain = inMemoryPersistence.getDeviceSearchDomain();
        Optional<SearchableProperty> searchableProperty = deviceSearchDomain.getProperties().stream().filter(p -> property.equals(p.getName())).findFirst();
        if (searchableProperty.isPresent()) {
            SearchablePropertyValue.ValueBean valueBean = new SearchablePropertyValue.ValueBean();
            valueBean.operator = operator;
            valueBean.values = values;
            SearchablePropertyValue searchablePropertyValue = new SearchablePropertyValue(searchableProperty.get());
            searchablePropertyValue.setValueBean(valueBean);
            return searchablePropertyValue;
        }
        throw new IllegalArgumentException("Searchable property with name '" + property + "' is not found");
    }
}
