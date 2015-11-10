package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.tests.rules.Expected;
import com.elster.jupiter.events.impl.EventServiceImpl;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EndDeviceGroupBuilder;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyOperator;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.events.EndDeviceGroupDeletionVetoEventHandler;
import com.energyict.mdc.device.data.impl.events.VetoDeleteDeviceGroupException;
import com.energyict.mdc.device.data.impl.search.DeviceSearchDomain;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import org.joda.time.DateTime;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class DeviceGroupTest extends PersistenceIntegrationTest {

    private static final String QUERY_EDG_NAME = "QueryEndDeviceGroup";
    private static final String ENUM_EDG_NAME = "EnumeratedEndDeviceGroup";

    private static final String ED_MRID1 = "mrid1";
    private static final String ED_MRID2 = "mrid2";
    private static final String ED_MRID3 = "xxx";
    private static final String DEVICE_NAME1 = "devicename1";
    private static final String DEVICE_NAME2 = "devicename2";
    private static final String DEVICE_NAME3 = "devicename3";

    @Test
    @Transactional
    public void testPersistenceDynamicGroup() {
        Device device1 = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME1, ED_MRID1);
        device1.save();
        Device device2 = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME2, ED_MRID2);
        device2.save();
        Device device3 = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME3, ED_MRID3);
        device3.save();
        EndDeviceGroupBuilder.QueryEndDeviceGroupBuilder builder = inMemoryPersistence.getMeteringGroupsService().createQueryEndDeviceGroup();
        builder.setMRID(QUERY_EDG_NAME);
        builder.setQueryProviderName(DeviceEndDeviceQueryProvider.DEVICE_ENDDEVICE_QUERYPROVIDER);
        builder.setSearchDomain(inMemoryPersistence.getDeviceSearchDomain());
        builder.withConditions(buildSearchablePropertyCondition("mRID", SearchablePropertyOperator.EQUAL, Collections.singletonList("mrid*")));
        builder.create();

        //Business method
        Optional<EndDeviceGroup> found = inMemoryPersistence.getMeteringGroupsService().findEndDeviceGroup(QUERY_EDG_NAME);

        //Asserts
        assertThat(found).isPresent();
        assertThat(found.get()).isInstanceOf(QueryEndDeviceGroup.class);
        QueryEndDeviceGroup group = (QueryEndDeviceGroup) found.get();
        List<EndDevice> members = group.getMembers(new DateTime(2014, 1, 23, 14, 54).toDate().toInstant());
        assertThat(members).hasSize(2);
        assertThat(members.get(0).getAmrId()).isEqualTo(String.valueOf(device1.getId()));
        assertThat(members.get(1).getAmrId()).isEqualTo(String.valueOf(device2.getId()));
    }

    @Test
    @Transactional
    public void testPersistenceStaticGroup() {
        Device device1 = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME1, ED_MRID1);
        device1.save();
        Device device2 = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME2, ED_MRID2);
        device2.save();
        Device device3 = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, DEVICE_NAME3, ED_MRID3);
        device3.save();
        EndDeviceGroupBuilder.EnumeratedEndDeviceGroupBuilder builder = inMemoryPersistence.getMeteringGroupsService().createEnumeratedEndDeviceGroup();
        builder.setMRID(ENUM_EDG_NAME);
        builder.containing(
                inMemoryPersistence.getMeteringService().findEndDevice(ED_MRID1).get(),
                inMemoryPersistence.getMeteringService().findEndDevice(ED_MRID2).get());
        builder.at(Instant.EPOCH);
        builder.create();

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
        Thesaurus thesaurus = inMemoryPersistence.getThesaurus();
        MeteringGroupsService meteringGroupsService = inMemoryPersistence.getMeteringGroupsService();
        ((EventServiceImpl)inMemoryPersistence.getEventService()).addTopicHandler(new EndDeviceGroupDeletionVetoEventHandler(kpiService, thesaurus));

        QueryEndDeviceGroup queryEndDeviceGroup = meteringGroupsService.createQueryEndDeviceGroup()
                .setMRID("mine")
                .setName("mine")
                .setQueryProviderName(DeviceEndDeviceQueryProvider.DEVICE_ENDDEVICE_QUERYPROVIDER)
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
        DeviceSearchDomain deviceSearchDomain = inMemoryPersistence.getDeviceSearchDomain();
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
