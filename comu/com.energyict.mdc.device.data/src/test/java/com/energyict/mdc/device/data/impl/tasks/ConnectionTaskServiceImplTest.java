package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.device.data.impl.PersistenceIntegrationTest;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import java.util.Map;
import org.junit.Test;

/**
 * Created by bvn on 12/22/14.
 */
public class ConnectionTaskServiceImplTest extends PersistenceIntegrationTest {

    @Test
    @Transactional
    public void testConnectionTaskStatusCount() throws Exception {
        QueryEndDeviceGroup queryEndDeviceGroup = inMemoryPersistence.getMeteringGroupsService().createQueryEndDeviceGroup(Where.where("deviceConfiguration.deviceType.name").isEqualTo("test"));
        queryEndDeviceGroup.save();
        Map<TaskStatus, Long> connectionTaskStatusCount = inMemoryPersistence.getConnectionTaskService().getConnectionTaskStatusCount(queryEndDeviceGroup);
    }
}
