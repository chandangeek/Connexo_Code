package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFilterSpecification;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.model.ComScheduleBuilder;
import com.energyict.mdc.tasks.ComTask;
import org.junit.Test;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test of the {@link DeviceServiceImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-25 (09:32)
 */
public class DeviceDataServiceImplIT extends PersistenceIntegrationTest {

    @Transactional
    @Test
    public void countConnectionTasksLastComSessionsWithAtLeastOneFailedTaskDoesNotProduceSQLExceptions() {
        // Business method
        inMemoryPersistence.getConnectionTaskService().countConnectionTasksLastComSessionsWithAtLeastOneFailedTask();

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void countWaitingConnectionTasksLastComSessionsWithAtLeastOneFailedTaskDoesNotProduceSQLExceptions() {
        // Business method
        inMemoryPersistence.getConnectionTaskService().countWaitingConnectionTasksLastComSessionsWithAtLeastOneFailedTask();

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getConnectionTaskLastComSessionSuccessIndicatorCountDoesNotProduceSQLExceptions() {
        // Business method
        inMemoryPersistence.getConnectionTaskService().getConnectionTaskLastComSessionSuccessIndicatorCount();

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getComTaskLastComSessionHighestPriorityCompletionCodeCountDoesNotProduceSQLExceptions() {
        // Business method
        inMemoryPersistence.getCommunicationTaskService().getComTaskLastComSessionHighestPriorityCompletionCodeCount();

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getConnectionTypeHeatMapDoesNotProduceSQLExceptions() {
        // Business method
        inMemoryPersistence.getConnectionTaskService().getConnectionTypeHeatMap();

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getConnectionsDeviceTypeHeatMapDoesNotProduceSQLExceptions() {
        // Business method
        inMemoryPersistence.getConnectionTaskService().getConnectionsDeviceTypeHeatMap();

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getComTasksDeviceTypeHeatMapDoesNotProduceSQLExceptions() {
        // Business method
        inMemoryPersistence.getCommunicationTaskService().getComTasksDeviceTypeHeatMap();

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getConnectionsComPortPoolHeatMapDoesNotProduceSQLExceptions() {
        // Business method
        inMemoryPersistence.getConnectionTaskService().getConnectionsComPortPoolHeatMap();

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