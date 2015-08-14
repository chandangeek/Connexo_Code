package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.tasks.TaskStatus;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;

import java.util.EnumSet;
import java.util.Set;

import org.junit.*;

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
    public void getComTasksDeviceTypeHeatMapDoesNotProduceSQLExceptions() {
        // Business method
        inMemoryPersistence.getCommunicationTaskService().getComTasksDeviceTypeHeatMap();

        // Asserts: should not cause any SQLExceptions
    }

}