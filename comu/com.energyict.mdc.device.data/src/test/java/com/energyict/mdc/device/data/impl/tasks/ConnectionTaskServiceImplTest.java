package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.DeviceEndDeviceQueryProvider;
import com.energyict.mdc.device.data.impl.PersistenceIntegrationTest;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.google.common.collect.BoundType;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Optional;
import org.junit.Test;

import static com.elster.jupiter.util.conditions.Where.where;
import static com.google.common.collect.Range.range;

/**
 * Created by bvn on 12/22/14.
 */
public class ConnectionTaskServiceImplTest extends PersistenceIntegrationTest {

    @Test
    @Transactional
    public void testConnectionTypeHeatMapWithQueryEndDeviceGroup() throws Exception {
        QueryEndDeviceGroup queryEndDeviceGroup = findOrCreateQueryEndDeviceGroup();
        inMemoryPersistence.getConnectionTaskService().getConnectionTypeHeatMap(queryEndDeviceGroup);
    }

    @Test
    @Transactional
    public void testConnectionsDeviceTypeHeatMapWithQueryEndDeviceGroup() throws Exception {
        QueryEndDeviceGroup queryEndDeviceGroup = findOrCreateQueryEndDeviceGroup();
        inMemoryPersistence.getConnectionTaskService().getConnectionsDeviceTypeHeatMap(queryEndDeviceGroup);
    }

    @Test
    @Transactional
    public void testConnectionsDeviceTypeHeatMapWithEnumeratedEndDeviceGroup() throws Exception {
        EnumeratedEndDeviceGroup enumeratedEndDeviceGroup = findOrCreateEnumeratedEndDeviceGroup();
        inMemoryPersistence.getConnectionTaskService().getConnectionsDeviceTypeHeatMap(enumeratedEndDeviceGroup);
    }

    @Test
    @Transactional
    public void testConnectionsComPortPoolHeatMapWithQueryEndDeviceGroup() throws Exception {
        QueryEndDeviceGroup queryEndDeviceGroup = findOrCreateQueryEndDeviceGroup();
        inMemoryPersistence.getConnectionTaskService().getConnectionsComPortPoolHeatMap(queryEndDeviceGroup);
    }

    @Test
    @Transactional
    public void testConnectionsComPortPoolHeatMapWithEnumeratedEndDeviceGroup() throws Exception {
        EnumeratedEndDeviceGroup enumeratedEndDeviceGroup = findOrCreateEnumeratedEndDeviceGroup();
        inMemoryPersistence.getConnectionTaskService().getConnectionsComPortPoolHeatMap(enumeratedEndDeviceGroup);
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
