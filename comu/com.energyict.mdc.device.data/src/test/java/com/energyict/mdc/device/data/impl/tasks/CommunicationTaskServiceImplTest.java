package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyOperator;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.DeviceEndDeviceQueryProvider;
import com.energyict.mdc.device.data.impl.PersistenceIntegrationTest;

import com.google.common.collect.BoundType;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import static com.google.common.collect.Range.range;

/**
 * Primary goal of these tests is to verify there are no sql exceptions along the way, hence the lack of asserts
 */
public class CommunicationTaskServiceImplTest extends PersistenceIntegrationTest {

    @Test
    @Transactional
    public void testComTaskLastComSessionHighestPriorityCompletionCodeCountWithQueryEndDeviceGroup() throws Exception {
        QueryEndDeviceGroup queryEndDeviceGroup = findOrCreateQueryEndDeviceGroup();
        inMemoryPersistence.getCommunicationTaskReportService().getComTaskLastComSessionHighestPriorityCompletionCodeCount(queryEndDeviceGroup);
    }

    @Test
    @Transactional
    public void testComTaskLastComSessionHighestPriorityCompletionCodeCountWithEnumeratedEndDeviceGroup() throws Exception {
        EnumeratedEndDeviceGroup queryEndDeviceGroup = findOrCreateEnumeratedEndDeviceGroup();
        inMemoryPersistence.getCommunicationTaskReportService().getComTaskLastComSessionHighestPriorityCompletionCodeCount(queryEndDeviceGroup);
    }

    @Test
    @Transactional
    public void testComTasksDeviceTypeHeatMapEnumeratedEndDeviceGroup() throws Exception {
        EnumeratedEndDeviceGroup queryEndDeviceGroup = findOrCreateEnumeratedEndDeviceGroup();
        inMemoryPersistence.getCommunicationTaskReportService().getComTasksDeviceTypeHeatMap(queryEndDeviceGroup);
    }

    private EnumeratedEndDeviceGroup findOrCreateEnumeratedEndDeviceGroup() {
        Optional<EndDeviceGroup> endDeviceGroup = inMemoryPersistence.getMeteringGroupsService().findEndDeviceGroup("static");
        if (endDeviceGroup.isPresent()) {
            return (EnumeratedEndDeviceGroup) endDeviceGroup.get();
        } else {
            EnumeratedEndDeviceGroup enumeratedEndDeviceGroup = inMemoryPersistence.getMeteringGroupsService().createEnumeratedEndDeviceGroup()
                    .setName("myDevices")
                    .setMRID("static")
                    .create();
            Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "myDevice", "ZAFO007", Instant.now());
            device.save();
            device.addToGroup(enumeratedEndDeviceGroup, range(Instant.EPOCH, BoundType.CLOSED, Instant.now(), BoundType.OPEN));
            return enumeratedEndDeviceGroup;
        }
    }


    private QueryEndDeviceGroup findOrCreateQueryEndDeviceGroup() {
        Optional<EndDeviceGroup> endDeviceGroup = inMemoryPersistence.getMeteringGroupsService().findEndDeviceGroup("dynamic");
        if (endDeviceGroup.isPresent()) {
            return (QueryEndDeviceGroup) endDeviceGroup.get();
        } else {
            QueryEndDeviceGroup queryEndDeviceGroup = inMemoryPersistence.getMeteringGroupsService().createQueryEndDeviceGroup()
                    .setName("dynamic")
                    .setSearchDomain(inMemoryPersistence.getDeviceSearchDomain())
                    .setQueryProviderName(DeviceEndDeviceQueryProvider.DEVICE_ENDDEVICE_QUERYPROVIDER)
                    .withConditions(buildSearchablePropertyCondition("mRID", SearchablePropertyOperator.EQUAL, Collections.singletonList("SPE*")))
                    .create();
            return queryEndDeviceGroup;
        }
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
