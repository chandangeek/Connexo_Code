package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.util.time.UtcInstant;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.scheduling.TemporalExpression;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.model.ComScheduleBuilder;
import com.energyict.mdc.tasks.ComTask;
import org.junit.Test;

import java.util.Date;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test of the {@link DeviceDataServiceImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-25 (09:32)
 */
public class DeviceDataServiceImplIT extends PersistenceIntegrationTest {

    @Transactional
    @Test
    public void testGetComTaskExecutionStatusCount() {
        // Business method
        Map<TaskStatus, Long> statusCount = inMemoryPersistence.getDeviceDataService().getComTaskExecutionStatusCount();

        // Assertts
        assertThat(statusCount).isNotNull();
        for (TaskStatus taskStatus : TaskStatus.values()) {
            assertThat(statusCount.get(taskStatus)).isNotNull();
        }
    }

    @Transactional
    @Test
    public void testGetCommunicationTasksDeviceTypeBreakdown() {
        ComTaskExecutionFilterSpecification filter = new ComTaskExecutionFilterSpecification();
        filter.taskStatuses = this.breakdownStatusses();
        filter.deviceTypes.add(deviceType);

        // Business method
        Map<TaskStatus, Long> statusCount = inMemoryPersistence.getDeviceDataService().getComTaskExecutionStatusCount(filter);

        // Assertts
        assertThat(statusCount).isNotNull();
        for (TaskStatus taskStatus : TaskStatus.values()) {
            assertThat(statusCount.get(taskStatus)).isNotNull();
        }
    }

    @Transactional
    @Test
    public void testGetCommunicationTasksComScheduleBreakdown() {
        ComScheduleBuilder scheduleBuilder =
                inMemoryPersistence.getSchedulingService().newComSchedule(
                        "testGetCommunicationTasksComScheduleBreakdown",
                        new TemporalExpression(TimeDuration.days(1)),
                        new UtcInstant(new Date()));
        ComSchedule comSchedule = scheduleBuilder.build();

        ComTask simpleComTask = inMemoryPersistence.getTaskService().newComTask("Simple task");
        simpleComTask.createStatusInformationTask();
        simpleComTask.save();
        comSchedule.addComTask(simpleComTask);
        comSchedule.save();

        ComTaskExecutionFilterSpecification filter = new ComTaskExecutionFilterSpecification();
        filter.taskStatuses = this.breakdownStatusses();
        filter.comSchedules.add(comSchedule);

        // Business method
        Map<TaskStatus, Long> statusCount = inMemoryPersistence.getDeviceDataService().getComTaskExecutionStatusCount(filter);

        // Assertts
        assertThat(statusCount).isNotNull();
        for (TaskStatus taskStatus : TaskStatus.values()) {
            assertThat(statusCount.get(taskStatus)).isNotNull();
        }
    }

    @Transactional
    @Test
    public void testGetCommunicationTasksBreakdown() {
        ComTask comTask = inMemoryPersistence.getTaskService().newComTask("testGetCommunicationTasksBreakdown");
        comTask.createBasicCheckTask().add();
        comTask.save();
        ComTaskExecutionFilterSpecification filter = new ComTaskExecutionFilterSpecification();
        filter.taskStatuses = this.breakdownStatusses();
        filter.comTasks.add(comTask);

        // Business method
        Map<TaskStatus, Long> statusCount = inMemoryPersistence.getDeviceDataService().getComTaskExecutionStatusCount(filter);

        // Assertts
        assertThat(statusCount).isNotNull();
        for (TaskStatus taskStatus : TaskStatus.values()) {
            assertThat(statusCount.get(taskStatus)).isNotNull();
        }
    }

    @Transactional
    @Test
    public void countConnectionTasksLastComSessionsWithAtLeastOneFailedTaskDoesNotProduceSQLExceptions() {
        // Business method
        inMemoryPersistence.getDeviceDataService().countConnectionTasksLastComSessionsWithAtLeastOneFailedTask();

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void countWaitingConnectionTasksLastComSessionsWithAtLeastOneFailedTaskDoesNotProduceSQLExceptions() {
        // Business method
        inMemoryPersistence.getDeviceDataService().countWaitingConnectionTasksLastComSessionsWithAtLeastOneFailedTask();

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getConnectionTaskLastComSessionSuccessIndicatorCountDoesNotProduceSQLExceptions() {
        // Business method
        inMemoryPersistence.getDeviceDataService().getConnectionTaskLastComSessionSuccessIndicatorCount();

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getComTaskLastComSessionHighestPriorityCompletionCodeCountDoesNotProduceSQLExceptions() {
        // Business method
        inMemoryPersistence.getDeviceDataService().getComTaskLastComSessionHighestPriorityCompletionCodeCount();

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getConnectionTypeHeatMapDoesNotProduceSQLExceptions() {
        // Business method
        inMemoryPersistence.getDeviceDataService().getConnectionTypeHeatMap();

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getConnectionsDeviceTypeHeatMapDoesNotProduceSQLExceptions() {
        // Business method
        inMemoryPersistence.getDeviceDataService().getConnectionsDeviceTypeHeatMap();

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getComTasksDeviceTypeHeatMapDoesNotProduceSQLExceptions() {
        // Business method
        inMemoryPersistence.getDeviceDataService().getComTasksDeviceTypeHeatMap();

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getConnectionsComPortPoolHeatMapDoesNotProduceSQLExceptions() {
        // Business method
        inMemoryPersistence.getDeviceDataService().getConnectionsComPortPoolHeatMap();

        // Asserts: should not cause any SQLExceptions
    }

    private Set<TaskStatus> breakdownStatusses() {
        Set<TaskStatus> taskStatuses = EnumSet.noneOf(TaskStatus.class);
        taskStatuses.addAll(EnumSet.of(TaskStatus.Waiting));
        taskStatuses.addAll(EnumSet.of(TaskStatus.Failed, TaskStatus.NeverCompleted));
        taskStatuses.addAll(EnumSet.of(TaskStatus.Pending, TaskStatus.Busy, TaskStatus.Retrying));
        return taskStatuses;
    }

}