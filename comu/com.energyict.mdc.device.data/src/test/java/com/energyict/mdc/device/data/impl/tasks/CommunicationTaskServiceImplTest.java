package com.energyict.mdc.device.data.impl.tasks;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.DeviceEndDeviceQueryProvider;
import com.energyict.mdc.device.data.impl.PersistenceIntegrationTest;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.google.common.collect.BoundType;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;

import static com.elster.jupiter.util.conditions.Where.where;
import static com.google.common.collect.Range.range;

/**
 * Primary goal of these tests is to verify there are no sql exceptions along the way, hence the lack of asserts
 */
public class CommunicationTaskServiceImplTest extends PersistenceIntegrationTest {

    @Test
    @Transactional
    public void testComTaskExecutionStatusCountForQueryEndDeviceGroup() throws Exception {
        QueryEndDeviceGroup queryEndDeviceGroup =  findOrCreateQueryEndDeviceGroup();
        Map<TaskStatus, Long> statusCount = inMemoryPersistence.getCommunicationTaskService().getComTaskExecutionStatusCount(queryEndDeviceGroup);
    }

    @Test
    @Transactional
    public void testComTaskExecutionStatusCountForEnumeratedEndDeviceGroup() throws Exception {
        EnumeratedEndDeviceGroup enumeratedEndDeviceGroup = findOrCreateEnumeratedEndDeviceGroup();
        Map<TaskStatus, Long> statusCount = inMemoryPersistence.getCommunicationTaskService().getComTaskExecutionStatusCount(enumeratedEndDeviceGroup);
    }

    @Test
    @Transactional
    public void testCommunicationTasksComScheduleBreakdownWithQueryEndDeviceGroup() throws Exception {
        QueryEndDeviceGroup queryEndDeviceGroup =  findOrCreateQueryEndDeviceGroup();
        Map<ComSchedule, Map<TaskStatus, Long>> map = inMemoryPersistence.getCommunicationTaskService().getCommunicationTasksComScheduleBreakdown(EnumSet.of(TaskStatus.Busy), queryEndDeviceGroup);
    }

    @Test
    @Transactional
    public void testCommunicationTasksComScheduleBreakdownWithEnumeratedEndDeviceGroup() throws Exception {
        EnumeratedEndDeviceGroup enumeratedEndDeviceGroup = findOrCreateEnumeratedEndDeviceGroup();
        Map<TaskStatus, Long> statusCount = inMemoryPersistence.getCommunicationTaskService().getComTaskExecutionStatusCount(enumeratedEndDeviceGroup);
    }

    @Test
    @Transactional
    public void testCommunicationTasksDeviceTypeBreakdownWithQueryEndDeviceGroup() throws Exception {
        QueryEndDeviceGroup queryEndDeviceGroup =  findOrCreateQueryEndDeviceGroup();
        Map<DeviceType, Map<TaskStatus, Long>> map = inMemoryPersistence.getCommunicationTaskService().getCommunicationTasksDeviceTypeBreakdown(EnumSet.of(TaskStatus.Busy), queryEndDeviceGroup);
    }

    @Test
    @Transactional
    public void testCommunicationTasksDeviceTypeBreakdownWithEnumeratedEndDeviceGroup() throws Exception {
        EnumeratedEndDeviceGroup enumeratedEndDeviceGroup = findOrCreateEnumeratedEndDeviceGroup();
        Map<DeviceType, Map<TaskStatus, Long>> map = inMemoryPersistence.getCommunicationTaskService().getCommunicationTasksDeviceTypeBreakdown(EnumSet.of(TaskStatus.Busy), enumeratedEndDeviceGroup);
    }

    @Test
    @Transactional
    public void testComTaskLastComSessionHighestPriorityCompletionCodeCountWithQueryEndDeviceGroup() throws Exception {
        QueryEndDeviceGroup queryEndDeviceGroup = findOrCreateQueryEndDeviceGroup();
        Map<CompletionCode, Long> comTaskLastComSessionHighestPriorityCompletionCodeCount = inMemoryPersistence.getCommunicationTaskService().getComTaskLastComSessionHighestPriorityCompletionCodeCount(queryEndDeviceGroup);
    }

    @Test
    @Transactional
    public void testComTaskLastComSessionHighestPriorityCompletionCodeCountWithEnumeratedEndDeviceGroup() throws Exception {
        EnumeratedEndDeviceGroup queryEndDeviceGroup = findOrCreateEnumeratedEndDeviceGroup();
        Map<CompletionCode, Long> comTaskLastComSessionHighestPriorityCompletionCodeCount = inMemoryPersistence.getCommunicationTaskService().getComTaskLastComSessionHighestPriorityCompletionCodeCount(queryEndDeviceGroup);
    }

    @Test
    @Transactional
    public void testComTasksDeviceTypeHeatMapWithQueryEndDeviceGroup() throws Exception {
        QueryEndDeviceGroup queryEndDeviceGroup = findOrCreateQueryEndDeviceGroup();
        Map<DeviceType, List<Long>> comTaskLastComSessionHighestPriorityCompletionCodeCount = inMemoryPersistence.getCommunicationTaskService().getComTasksDeviceTypeHeatMap(queryEndDeviceGroup);
    }

    @Test
    @Transactional
    public void testComTasksDeviceTypeHeatMapEnumeratedEndDeviceGroup() throws Exception {
        EnumeratedEndDeviceGroup queryEndDeviceGroup = findOrCreateEnumeratedEndDeviceGroup();
        Map<DeviceType, List<Long>> comTaskLastComSessionHighestPriorityCompletionCodeCount = inMemoryPersistence.getCommunicationTaskService().getComTasksDeviceTypeHeatMap(queryEndDeviceGroup);
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
