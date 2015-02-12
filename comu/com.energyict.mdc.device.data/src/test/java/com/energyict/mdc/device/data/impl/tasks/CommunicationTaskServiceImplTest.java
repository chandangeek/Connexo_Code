package com.energyict.mdc.device.data.impl.tasks;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.DeviceEndDeviceQueryProvider;
import com.energyict.mdc.device.data.impl.PersistenceIntegrationTest;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.google.common.collect.BoundType;

import java.time.Instant;
import java.util.Optional;

import org.junit.*;

import static com.elster.jupiter.util.conditions.Where.where;
import static com.google.common.collect.Range.range;

/**
 * Primary goal of these tests is to verify there are no sql exceptions along the way, hence the lack of asserts
 */
public class CommunicationTaskServiceImplTest extends PersistenceIntegrationTest {

    @Test
    @Transactional
    public void testComTaskLastComSessionHighestPriorityCompletionCodeCountWithQueryEndDeviceGroup() throws Exception {
        QueryEndDeviceGroup queryEndDeviceGroup = findOrCreateQueryEndDeviceGroup();
        inMemoryPersistence.getCommunicationTaskService().getComTaskLastComSessionHighestPriorityCompletionCodeCount(queryEndDeviceGroup);
    }

    @Test
    @Transactional
    public void testComTaskLastComSessionHighestPriorityCompletionCodeCountWithEnumeratedEndDeviceGroup() throws Exception {
        EnumeratedEndDeviceGroup queryEndDeviceGroup = findOrCreateEnumeratedEndDeviceGroup();
        inMemoryPersistence.getCommunicationTaskService().getComTaskLastComSessionHighestPriorityCompletionCodeCount(queryEndDeviceGroup);
    }

    @Test
    @Transactional
    public void testComTasksDeviceTypeHeatMapEnumeratedEndDeviceGroup() throws Exception {
        EnumeratedEndDeviceGroup queryEndDeviceGroup = findOrCreateEnumeratedEndDeviceGroup();
        inMemoryPersistence.getCommunicationTaskService().getComTasksDeviceTypeHeatMap(queryEndDeviceGroup);
    }

    private EnumeratedEndDeviceGroup findOrCreateEnumeratedEndDeviceGroup() {
        Optional<EndDeviceGroup> endDeviceGroup = inMemoryPersistence.getMeteringGroupsService().findEndDeviceGroup("static");
        if (endDeviceGroup.isPresent()) {
            return (EnumeratedEndDeviceGroup)endDeviceGroup.get();
        } else {
            EnumeratedEndDeviceGroup enumeratedEndDeviceGroup = inMemoryPersistence.getMeteringGroupsService().createEnumeratedEndDeviceGroup("myDevices");
            Device device = inMemoryPersistence.getDeviceService().newDevice(deviceConfiguration, "myDevice", "ZAFO007");
            device.save();
            device.addToGroup(enumeratedEndDeviceGroup, range(Instant.EPOCH, BoundType.CLOSED, Instant.now(), BoundType.OPEN));
            enumeratedEndDeviceGroup.setMRID("static");
            enumeratedEndDeviceGroup.setQueryProviderName(DeviceEndDeviceQueryProvider.DEVICE_ENDDEVICE_QUERYPROVIDER);
            enumeratedEndDeviceGroup.save();
            return enumeratedEndDeviceGroup;
        }
    }


    private QueryEndDeviceGroup findOrCreateQueryEndDeviceGroup() {
        Optional<EndDeviceGroup> endDeviceGroup = inMemoryPersistence.getMeteringGroupsService().findEndDeviceGroup("dynamic");
        if (endDeviceGroup.isPresent()) {
            return (QueryEndDeviceGroup)endDeviceGroup.get();
        } else {
            Condition conditionDevice = Condition.TRUE.and(where("deviceConfiguration.deviceType.name").isEqualTo("myType"));
            QueryEndDeviceGroup queryEndDeviceGroup = inMemoryPersistence.getMeteringGroupsService().createQueryEndDeviceGroup(conditionDevice);
            queryEndDeviceGroup.setMRID("dynamic");
            queryEndDeviceGroup.setQueryProviderName(DeviceEndDeviceQueryProvider.DEVICE_ENDDEVICE_QUERYPROVIDER);
            queryEndDeviceGroup.save();
            return queryEndDeviceGroup;
        }
    }
}
